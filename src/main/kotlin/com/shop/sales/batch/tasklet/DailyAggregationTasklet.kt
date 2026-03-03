package com.shop.sales.batch.tasklet

import com.shop.common.batch.JobParam
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DailyAggregationTasklet(
    private val jdbc: NamedParameterJdbcTemplate,
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val jobParams = chunkContext.stepContext.jobParameters
        val bizDate: LocalDate = JobParam.requireBusinessDate(jobParams[JobParam.BUSINESS_DATE] as String?)

        jdbc.update(
            """
            insert into tb_sales_daily(biz_date, brand_id, item_code, order_cnt, qty_sum, amount_sum, updated_at)
            select
                s.business_date,
                s.brand_id,
                s.item_code,
                count(*) as order_cnt,
                sum(s.qty)::bigint as qty_sum,
                sum(s.amount) as amount_sum,
                now()
            from tb_sales_settlement s
            where s.business_date = :bizDate
            group by s.business_date, s.brand_id, s.item_code
            on conflict (biz_date, brand_id, item_code)
            do update set
                order_cnt = excluded.order_cnt,
                qty_sum = excluded.qty_sum,
                amount_sum = excluded.amount_sum,
                updated_at = now()
            """.trimIndent(),
            mapOf("bizDate" to bizDate)
        )

        return RepeatStatus.FINISHED
    }
}