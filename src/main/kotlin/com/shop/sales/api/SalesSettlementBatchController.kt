package com.shop.sales.api

import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/batch/sales-settlement")
class SalesSettlementBatchController(
    private val jobOperator: JobOperator,
    private val jobRepository: JobRepository,
    @Qualifier("salesSettlementJob")
    private val salesSettlementJob: Job,
) {

    /**
     * 강제 실행
     * 예) POST /batch/sales-settlement/run?businessDate=2026-03-02
     */
    @PostMapping("/run")
    fun run(@RequestParam businessDate: String): ResponseEntity<Any> {
        val bizDate = try {
            LocalDate.parse(businessDate) // yyyy-MM-dd
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "message" to "Invalid businessDate. expected yyyy-MM-dd",
                    "businessDate" to businessDate
                )
            )
        }

        // 중복 실행 방지 (실행 중이면 409)
        val running = jobRepository.findRunningJobExecutions(salesSettlementJob.name)
        if (running.isNotEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                mapOf(
                    "message" to "Job is already running",
                    "jobName" to salesSettlementJob.name,
                    "runningExecutionIds" to running.mapNotNull { it.id }
                )
            )
        }

        // businessDate 반드시 포함 + identifying=true 명시(누락/미표시 방지)
        val params = JobParametersBuilder()
            .addString("businessDate", bizDate.toString(), true)
            .addLong("run.id", System.currentTimeMillis(), true) // 재실행용 유니크 키
            .toJobParameters()

        val execution = jobOperator.start(salesSettlementJob, params)

        return ResponseEntity.ok(
            mapOf(
                "jobName" to execution.jobInstance.jobName,
                "executionId" to execution.id,
                "status" to execution.status.toString(),
                "exitStatus" to execution.exitStatus.exitCode,
                "businessDate" to bizDate.toString()
            )
        )
    }
}