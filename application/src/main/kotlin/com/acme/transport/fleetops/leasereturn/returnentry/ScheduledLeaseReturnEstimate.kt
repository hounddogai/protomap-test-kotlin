package com.acme.transport.fleetops.leasereturn.returnentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled lease return estimate is in its life cycle. */
enum class ScheduledLeaseReturnEstimateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled lease return estimate. Its code is unique within a tenant. */
data class ScheduledLeaseReturnEstimate(
    var code: String,
    var label: String,
    var loadKg: Double,
    var batchSize: Int,
    var expiresAt: Int,
    var visible: Boolean,
    var ratio: Double,
    var trailerId: String,
    var lane: String,
    var status: ScheduledLeaseReturnEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLeaseReturnEstimateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLeaseReturnEstimateStatus): Boolean {
        when (status) {
            ScheduledLeaseReturnEstimateStatus.NEW -> return next == ScheduledLeaseReturnEstimateStatus.DONE || next == ScheduledLeaseReturnEstimateStatus.FAILED
            ScheduledLeaseReturnEstimateStatus.QUEUED -> return next == ScheduledLeaseReturnEstimateStatus.FAILED
            ScheduledLeaseReturnEstimateStatus.RUNNING -> return next == ScheduledLeaseReturnEstimateStatus.DONE
            ScheduledLeaseReturnEstimateStatus.DONE -> return next == ScheduledLeaseReturnEstimateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledLeaseReturnEstimateStatus) {
        if (!canMoveTo(next)) {
            error("scheduled lease return estimate $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedExposure(): Double = round(batchSize.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): ScheduledLeaseReturnEstimate {
        return ScheduledLeaseReturnEstimate(
            code = code,
            label = label,
            loadKg = loadKg,
            batchSize = batchSize,
            expiresAt = expiresAt,
            visible = visible,
            ratio = ratio,
            trailerId = trailerId,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled lease return estimate in status new. */
fun newScheduledLeaseReturnEstimate(code: String, label: String): ScheduledLeaseReturnEstimate {
    return ScheduledLeaseReturnEstimate(
        code = code,
        label = label,
        loadKg = 0.0,
        batchSize = 0,
        expiresAt = 0,
        visible = false,
        ratio = 0.0,
        trailerId = "",
        lane = "",
        status = ScheduledLeaseReturnEstimateStatus.NEW,
    )
}
