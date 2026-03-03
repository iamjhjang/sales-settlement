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
class MonthlyAggregationTasklet(
    private val jdbc: NamedParameterJdbcTemplate,
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        val raw = chunkContext.stepContext.stepExecution.jobParameters.getString(JobParam.BUSINESS_DATE)
        val bizDate = JobParam.requireBusinessDate(raw)

        val monthStart = bizDate.withDayOfMonth(1)
        val nextMonthStart = monthStart.plusMonths(1)

        jdbc.update(
            """
            insert into tb_sales_monthly(
                sales_month, brand_id, item_code, item_name,
                order_cnt, qty_sum, amount_sum, delivery_fee_sum,
                created_at, updated_at, created_by, updated_by
            )
            select
                to_char(s.sales_date, 'YYYY-MM') as sales_month,
                s.brand_id,
                s.item_code,
                max(s.item_name) as item_name,
                count(*)::int as order_cnt,
                sum(s.qty)::int as qty_sum,
                sum(s.amount) as amount_sum,
                sum(s.delivery_fee) as delivery_fee_sum,
                now(), now(), 'batch', 'batch'
            from tb_sales_detail s
            where s.sales_date >= :monthStart
              and s.sales_date <  :nextMonthStart
              and s.status = 'COMPLETED'
            group by to_char(s.sales_date, 'YYYY-MM'), s.brand_id, s.item_code
            on conflict (sales_month, brand_id, item_code)
            do update set
                item_name        = excluded.item_name,
                order_cnt        = excluded.order_cnt,
                qty_sum          = excluded.qty_sum,
                amount_sum       = excluded.amount_sum,
                delivery_fee_sum = excluded.delivery_fee_sum,
                updated_at       = now(),
                updated_by       = 'batch'
            """.trimIndent(),
            mapOf("monthStart" to monthStart, "nextMonthStart" to nextMonthStart)
        )

        return RepeatStatus.FINISHED
    }
}