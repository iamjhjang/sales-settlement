package com.shop.sales.batch.config

import com.shop.sales.batch.tasklet.*
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class SalesSettlementStepConfig(
    private val jobRepository: JobRepository,
    private val txManager: PlatformTransactionManager,
    private val orderSettlementTasklet: OrderSettlementTasklet,
    private val dailyAggregationTasklet: DailyAggregationTasklet,
    private val monthlyAggregationTasklet: MonthlyAggregationTasklet,
    private val yearlyAggregationTasklet: YearlyAggregationTasklet,
) {
    @Bean
    fun settleOrdersStep(): Step =
        StepBuilder("settleOrdersStep", jobRepository)
            .tasklet(orderSettlementTasklet, txManager)
            .build()

    @Bean
    fun aggregateDailySalesStep(): Step =
        StepBuilder("aggregateDailySalesStep", jobRepository)
            .tasklet(dailyAggregationTasklet, txManager)
            .build()

    @Bean
    fun aggregateMonthlySalesStep(): Step =
        StepBuilder("aggregateMonthlySalesStep", jobRepository)
            .tasklet(monthlyAggregationTasklet, txManager)
            .build()

    @Bean
    fun aggregateYearlySalesStep(): Step =
        StepBuilder("aggregateYearlySalesStep", jobRepository)
            .tasklet(yearlyAggregationTasklet, txManager)
            .build()
}