package com.shop.sales.batch.tasklet

import com.shop.common.batch.JobParam
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class OrderSettlementTasklet(
    private val jdbc: NamedParameterJdbcTemplate,
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {

        val raw = chunkContext.stepContext.stepExecution.jobParameters.getString(JobParam.BUSINESS_DATE)
        val bizDate = JobParam.requireBusinessDate(raw)

        val fromTs = bizDate.atStartOfDay()              // TIMESTAMP (no tz) - delivery_dt 타입과 일치
        val toTs = bizDate.plusDays(1).atStartOfDay()

        // 1) 원장(tb_sales_detail) 적재 (멱등: order_no UNIQUE)
        val upserted: Int = jdbc.update(
            """
            insert into tb_sales_detail(
                order_no, sales_date, brand_id, item_code, item_name,
                qty, amount, delivery_fee, status,
                created_at, updated_at, created_by, updated_by
            )
            select
                o.order_no,
                date(d.delivery_dt) as sales_date,
                o.brand_id,
                o.item_code,
                o.item_name,
                o.qty,
                o.amount,
                d.delivery_fee,
                o.status,
                now(), now(), 'batch', 'batch'
            from tb_order o
            join tb_delivery d on d.order_id = o.order_id
            where o.status = 'COMPLETED'
              and o.settled_at is null
              and d.delivery_dt >= :fromTs and d.delivery_dt < :toTs
            on conflict (order_no)
            do update set
                sales_date    = excluded.sales_date,
                brand_id      = excluded.brand_id,
                item_code     = excluded.item_code,
                item_name     = excluded.item_name,
                qty           = excluded.qty,
                amount        = excluded.amount,
                delivery_fee  = excluded.delivery_fee,
                status        = excluded.status,
                updated_at    = now(),
                updated_by    = 'batch'
            """.trimIndent(),
            mapOf("fromTs" to fromTs, "toTs" to toTs)
        )

        // 2) tb_order settled_at 업데이트 (원장에 들어간 주문만)
        val updatedOrders: Int = jdbc.update(
            """
            update tb_order o
               set settled_at = now(),
                   updated_at = now(),
                   updated_by = 'batch'
             where o.status = 'COMPLETED'
               and o.settled_at is null
               and exists (
                   select 1
                     from tb_sales_detail s
                    where s.order_no = o.order_no
                      and s.sales_date = :bizDate
               )
            """.trimIndent(),
            mapOf("bizDate" to bizDate)
        )

        // Spring Batch 6: writeCount는 long
        contribution.incrementWriteCount((upserted + updatedOrders).toLong())

        return RepeatStatus.FINISHED
    }
}