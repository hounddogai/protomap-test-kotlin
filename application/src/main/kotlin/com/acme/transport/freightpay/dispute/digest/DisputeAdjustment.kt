package com.acme.transport.freightpay.dispute.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute adjustment is in its life cycle. */
enum class DisputeAdjustmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One dispute adjustment, with what the planners need to act on it. */
data class DisputeAdjustment(
    var code: String,
    var label: String,
    var rate: Double,
    var graceDays: Int,
    var externalRef: String,
    var vehicleId: String,
    var ceiling: Double,
    var active: Boolean,
    var stopCount: Int,
    var limit: Int,
    var phone: String,
    var status: DisputeAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeAdjustmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeAdjustmentStatus): Boolean {
        when (status) {
            DisputeAdjustmentStatus.OPEN -> {
                when (next) {
                    DisputeAdjustmentStatus.RESOLVED, DisputeAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
            DisputeAdjustmentStatus.IN_REVIEW -> {
                when (next) {
                    DisputeAdjustmentStatus.REOPENED -> return true
                }
                return false
            }
            DisputeAdjustmentStatus.RESOLVED -> return next == DisputeAdjustmentStatus.REOPENED || next == DisputeAdjustmentStatus.ARCHIVED
            DisputeAdjustmentStatus.REOPENED -> return next == DisputeAdjustmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DisputeAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("dispute adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"dispute adjustment"} $code"

    fun adjustedExposure(): Double = round((ceiling - stopCount.toDouble()) * 100) / 100

    fun snapshot(): DisputeAdjustment {
        return DisputeAdjustment(
            code = code,
            label = label,
            rate = rate,
            graceDays = graceDays,
            externalRef = externalRef,
            vehicleId = vehicleId,
            ceiling = ceiling,
            active = active,
            stopCount = stopCount,
            limit = limit,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dispute adjustment in status open. */
fun newDisputeAdjustment(code: String, label: String): DisputeAdjustment {
    return DisputeAdjustment(
        code = code,
        label = label,
        rate = 0.0,
        graceDays = 0,
        externalRef = "",
        vehicleId = "",
        ceiling = 0.0,
        active = false,
        stopCount = 0,
        limit = 0,
        phone = "",
        status = DisputeAdjustmentStatus.OPEN,
    )
}
