package com.shop.sales.batch.tasklet

import com.shop.common.batch.JobParam
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class MonthlyAggregationTasklet(
    private val jdbc: NamedParameterJdbcTemplate,
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val raw = chunkContext.stepContext.stepExecution.jobParameters.getString(JobParam.BUSINESS_DATE)
        val bizDate = JobParam.businessDateOrToday(raw)

        val monthStart = bizDate.withDayOfMonth(1)
        val nextMonthStart = monthStart.plusMonths(1)

        jdbc.update(
            """
            insert into tb_sales_monthly(month_key, brand_id, item_code, order_cnt, qty_sum, amount_sum, updated_at)
            select
                to_char(s.business_date, 'YYYYMM') as month_key,
                s.brand_id,
                s.item_code,
                count(*) as order_cnt,
                sum(s.qty)::bigint as qty_sum,
                sum(s.amount) as amount_sum,
                now()
            from tb_sales_settlement s
            where s.business_date >= :monthStart and s.business_date < :nextMonthStart
            group by to_char(s.business_date, 'YYYYMM'), s.brand_id, s.item_code
            on conflict (month_key, brand_id, item_code)
            do update set
                order_cnt = excluded.order_cnt,
                qty_sum = excluded.qty_sum,
                amount_sum = excluded.amount_sum,
                updated_at = now()
            """.trimIndent(),
            mapOf("monthStart" to monthStart, "nextMonthStart" to nextMonthStart)
        )

        return RepeatStatus.FINISHED
    }
}