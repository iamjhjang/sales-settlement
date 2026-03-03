package com.shop.sales.batch.tasklet

import com.shop.common.batch.JobParam
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId

@Component
class OrderSettlementTasklet(
    private val jdbc: NamedParameterJdbcTemplate,
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        val raw = chunkContext.stepContext.stepExecution.jobParameters.getString("businessDate")
        val bizDate = JobParam.requireBusinessDate(raw)

        val zone = ZoneId.of("Asia/Seoul")
        val fromTs = bizDate.atStartOfDay(zone).toOffsetDateTime()
        val toTs = bizDate.plusDays(1).atStartOfDay(zone).toOffsetDateTime()

        val jobExecutionId = chunkContext.stepContext.stepExecution.jobExecutionId

        // 1) 정산원장 적재 (멱등)
        val inserted: Int = jdbc.update(
            """
            insert into tb_sales_settlement(
                order_id, order_no, brand_id, item_code, qty, unit_price, amount,
                delivered_at, business_date, job_execution_id
            )
            select
                o.order_id, o.order_no, o.brand_id, o.item_code, o.qty, o.unit_price, o.amount,
                o.delivered_at, date(o.delivered_at), :jobExecutionId
            from tb_order o
            where o.status = 'DELIVERED'
              and o.delivered_at >= :fromTs and o.delivered_at < :toTs
            on conflict (order_id) do nothing
            """.trimIndent(),
            mapOf(
                "fromTs" to fromTs,
                "toTs" to toTs,
                "jobExecutionId" to jobExecutionId
            )
        )

        // 2) 주문 settled_at 업데이트 (정산원장에 들어간 것만)
        val updated: Int = jdbc.update(
            """
            update tb_order o
               set settled_at = now()
             where o.status = 'DELIVERED'
               and o.settled_at is null
               and o.delivered_at >= :fromTs and o.delivered_at < :toTs
               and exists (select 1 from tb_sales_settlement s where s.order_id = o.order_id)
            """.trimIndent(),
            mapOf("fromTs" to fromTs, "toTs" to toTs)
        )

        contribution.incrementWriteCount((inserted + updated).toLong())

        return RepeatStatus.FINISHED
    }
}