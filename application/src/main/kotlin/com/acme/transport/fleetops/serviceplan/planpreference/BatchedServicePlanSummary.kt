package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched service plan summary is in its life cycle. */
enum class BatchedServicePlanSummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a batched service plan summary and its current status. */
data class BatchedServicePlanSummary(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var cost: Double,
    var vehicleId: String,
    var summary: String,
    var attempts: Int,
    var routeCode: String,
    var startsAt: Int,
    var visible: Boolean,
    var status: BatchedServicePlanSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedServicePlanSummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedServicePlanSummaryStatus): Boolean {
        when (status) {
            BatchedServicePlanSummaryStatus.NEW -> {
                when (next) {
                    BatchedServicePlanSummaryStatus.QUEUED, BatchedServicePlanSummaryStatus.FAILED -> return true
                }
                return false
            }
            BatchedServicePlanSummaryStatus.QUEUED -> return next == BatchedServicePlanSummaryStatus.RUNNING || next == BatchedServicePlanSummaryStatus.DONE
            BatchedServicePlanSummaryStatus.RUNNING -> return next == BatchedServicePlanSummaryStatus.DONE
            BatchedServicePlanSummaryStatus.DONE -> return next == BatchedServicePlanSummaryStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BatchedServicePlanSummaryStatus) {
        if (!canMoveTo(next)) {
            error("batched service plan summary $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedValue(): Double = round((cost + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedServicePlanSummary {
        return BatchedServicePlanSummary(
            code = code,
            label = label,
            expiresAt = expiresAt,
            cost = cost,
            vehicleId = vehicleId,
            summary = summary,
            attempts = attempts,
            routeCode = routeCode,
            startsAt = startsAt,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched service plan summary in status new. */
fun newBatchedServicePlanSummary(code: String, label: String): BatchedServicePlanSummary {
    return BatchedServicePlanSummary(
        code = code,
        label = label,
        expiresAt = 0,
        cost = 0.0,
        vehicleId = "",
        summary = "",
        attempts = 0,
        routeCode = "",
        startsAt = 0,
        visible = false,
        status = BatchedServicePlanSummaryStatus.NEW,
    )
}
