package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled dispute adjustment records of freightpay's dispute work, and the rules, stores, and
// calculations around them.

/** Where a scheduled dispute adjustment is in its life cycle. */
enum class ScheduledDisputeAdjustmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled dispute adjustment. Its code is unique within a tenant. */
data class ScheduledDisputeAdjustment(
    var code: String,
    var label: String,
    var driverId: String,
    var endsAt: Int,
    var updatedAt: Int,
    var vehicleId: String,
    var retries: Int,
    var depot: String,
    var status: ScheduledDisputeAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDisputeAdjustmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDisputeAdjustmentStatus): Boolean {
        when (status) {
            ScheduledDisputeAdjustmentStatus.DRAFT -> return next == ScheduledDisputeAdjustmentStatus.ACTIVE || next == ScheduledDisputeAdjustmentStatus.SUSPENDED
            ScheduledDisputeAdjustmentStatus.PENDING -> return next == ScheduledDisputeAdjustmentStatus.ACTIVE || next == ScheduledDisputeAdjustmentStatus.CLOSED
            ScheduledDisputeAdjustmentStatus.ACTIVE -> {
                when (next) {
                    ScheduledDisputeAdjustmentStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledDisputeAdjustmentStatus.SUSPENDED -> return next == ScheduledDisputeAdjustmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledDisputeAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dispute adjustment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun netTotal(): Double = round(retries.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): ScheduledDisputeAdjustment {
        return ScheduledDisputeAdjustment(
            code = code,
            label = label,
            driverId = driverId,
            endsAt = endsAt,
            updatedAt = updatedAt,
            vehicleId = vehicleId,
            retries = retries,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled dispute adjustment in status draft. */
fun newScheduledDisputeAdjustment(code: String, label: String): ScheduledDisputeAdjustment {
    return ScheduledDisputeAdjustment(
        code = code,
        label = label,
        driverId = "",
        endsAt = 0,
        updatedAt = 0,
        vehicleId = "",
        retries = 0,
        depot = "",
        status = ScheduledDisputeAdjustmentStatus.DRAFT,
    )
}
