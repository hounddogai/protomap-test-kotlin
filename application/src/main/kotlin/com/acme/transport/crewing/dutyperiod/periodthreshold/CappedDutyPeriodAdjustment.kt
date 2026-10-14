package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped duty period adjustment is in its life cycle. */
enum class CappedDutyPeriodAdjustmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One capped duty period adjustment, with what the planners need to act on it. */
data class CappedDutyPeriodAdjustment(
    var code: String,
    var name: String,
    var windowDays: Int,
    var ceiling: Double,
    var visible: Boolean,
    var quantity: Int,
    var batchSize: Int,
    var capacity: Int,
    var minimum: Double,
    var channel: String,
    var ownerId: String,
    var status: CappedDutyPeriodAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDutyPeriodAdjustmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDutyPeriodAdjustmentStatus): Boolean {
        when (status) {
            CappedDutyPeriodAdjustmentStatus.PLANNED -> return next == CappedDutyPeriodAdjustmentStatus.IN_PROGRESS || next == CappedDutyPeriodAdjustmentStatus.COMPLETED
            CappedDutyPeriodAdjustmentStatus.SCHEDULED -> return next == CappedDutyPeriodAdjustmentStatus.IN_PROGRESS
            CappedDutyPeriodAdjustmentStatus.IN_PROGRESS -> return next == CappedDutyPeriodAdjustmentStatus.COMPLETED || next == CappedDutyPeriodAdjustmentStatus.CANCELLED
            CappedDutyPeriodAdjustmentStatus.COMPLETED -> {
                when (next) {
                    CappedDutyPeriodAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedDutyPeriodAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("capped duty period adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round((windowDays.toDouble() - minimum) * 100) / 100

    fun snapshot(): CappedDutyPeriodAdjustment {
        return CappedDutyPeriodAdjustment(
            code = code,
            name = name,
            windowDays = windowDays,
            ceiling = ceiling,
            visible = visible,
            quantity = quantity,
            batchSize = batchSize,
            capacity = capacity,
            minimum = minimum,
            channel = channel,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capped duty period adjustment in status planned. */
fun newCappedDutyPeriodAdjustment(code: String, name: String): CappedDutyPeriodAdjustment {
    return CappedDutyPeriodAdjustment(
        code = code,
        name = name,
        windowDays = 0,
        ceiling = 0.0,
        visible = false,
        quantity = 0,
        batchSize = 0,
        capacity = 0,
        minimum = 0.0,
        channel = "",
        ownerId = "",
        status = CappedDutyPeriodAdjustmentStatus.PLANNED,
    )
}
