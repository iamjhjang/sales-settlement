package com.shop.sales.batch.config

import org.springframework.batch.core.job.Job
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.parameters.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SalesSettlementJobConfig(
    private val jobRepository: JobRepository,
    private val settleOrdersStep: Step,
    private val aggregateDailySalesStep: Step,
    private val aggregateMonthlySalesStep: Step,
    private val aggregateYearlySalesStep: Step,
) {
    @Bean
    fun salesSettlementJob(): Job =
        JobBuilder("salesSettlementJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(settleOrdersStep)
            .next(aggregateDailySalesStep)
            .next(aggregateMonthlySalesStep)
            .next(aggregateYearlySalesStep)
            .build()
}