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
class DailyAggregationTasklet(
    private val jdbc: NamedParameterJdbcTemplate,
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        val raw = chunkContext.stepContext.stepExecution.jobParameters.getString(JobParam.BUSINESS_DATE)
        val bizDate = JobParam.requireBusinessDate(raw)

        jdbc.update(
            """
            insert into tb_sales_daily(
                sales_day, brand_id, item_code, item_name,
                order_cnt, qty_sum, amount_sum, delivery_fee_sum,
                created_at, updated_at, created_by, updated_by
            )
            select
                s.sales_date as sales_day,
                s.brand_id,
                s.item_code,
                max(s.item_name) as item_name,
                count(*)::int as order_cnt,
                sum(s.qty)::int as qty_sum,
                sum(s.amount) as amount_sum,
                sum(s.delivery_fee) as delivery_fee_sum,
                now(), now(), 'batch', 'batch'
            from tb_sales_detail s
            where s.sales_date = :bizDate
              and s.status = 'COMPLETED'
            group by s.sales_date, s.brand_id, s.item_code
            on conflict (sales_day, brand_id, item_code)
            do update set
                item_name        = excluded.item_name,
                order_cnt        = excluded.order_cnt,
                qty_sum          = excluded.qty_sum,
                amount_sum       = excluded.amount_sum,
                delivery_fee_sum = excluded.delivery_fee_sum,
                updated_at       = now(),
                updated_by       = 'batch'
            """.trimIndent(),
            mapOf("bizDate" to bizDate)
        )

        return RepeatStatus.FINISHED
    }
}