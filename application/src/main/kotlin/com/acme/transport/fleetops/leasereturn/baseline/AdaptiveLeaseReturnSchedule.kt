package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive lease return schedule is in its life cycle. */
enum class AdaptiveLeaseReturnScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a adaptive lease return schedule and its current status. */
data class AdaptiveLeaseReturnSchedule(
    var code: String,
    var label: String,
    var stopCount: Int,
    var vehicleId: String,
    var summary: String,
    var quantity: Int,
    var status: AdaptiveLeaseReturnScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLeaseReturnScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLeaseReturnScheduleStatus): Boolean {
        when (status) {
            AdaptiveLeaseReturnScheduleStatus.DRAFT -> return next == AdaptiveLeaseReturnScheduleStatus.PENDING || next == AdaptiveLeaseReturnScheduleStatus.ACTIVE
            AdaptiveLeaseReturnScheduleStatus.PENDING -> return next == AdaptiveLeaseReturnScheduleStatus.ACTIVE || next == AdaptiveLeaseReturnScheduleStatus.CLOSED
            AdaptiveLeaseReturnScheduleStatus.ACTIVE -> {
                when (next) {
                    AdaptiveLeaseReturnScheduleStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveLeaseReturnScheduleStatus.SUSPENDED -> return next == AdaptiveLeaseReturnScheduleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveLeaseReturnScheduleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive lease return schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedTotal(): Double = round((stopCount.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveLeaseReturnSchedule {
        return AdaptiveLeaseReturnSchedule(
            code = code,
            label = label,
            stopCount = stopCount,
            vehicleId = vehicleId,
            summary = summary,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive lease return schedule in status draft. */
fun newAdaptiveLeaseReturnSchedule(code: String, label: String): AdaptiveLeaseReturnSchedule {
    return AdaptiveLeaseReturnSchedule(
        code = code,
        label = label,
        stopCount = 0,
        vehicleId = "",
        summary = "",
        quantity = 0,
        status = AdaptiveLeaseReturnScheduleStatus.DRAFT,
    )
}
