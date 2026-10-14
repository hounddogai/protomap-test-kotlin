package com.acme.transport.safety.violation.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global violation tier is in its life cycle. */
enum class GlobalViolationTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A global violation tier as the violation team keeps it. */
data class GlobalViolationTier(
    var code: String,
    var name: String,
    var batchSize: Int,
    var odometer: Int,
    var description: String,
    var lane: String,
    var endsAt: Int,
    var currency: String,
    var status: GlobalViolationTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalViolationTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalViolationTierStatus): Boolean {
        when (status) {
            GlobalViolationTierStatus.PLANNED -> return next == GlobalViolationTierStatus.COMPLETED
            GlobalViolationTierStatus.SCHEDULED -> return next == GlobalViolationTierStatus.IN_PROGRESS
            GlobalViolationTierStatus.IN_PROGRESS -> {
                when (next) {
                    GlobalViolationTierStatus.COMPLETED, GlobalViolationTierStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalViolationTierStatus.COMPLETED -> return next == GlobalViolationTierStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: GlobalViolationTierStatus) {
        if (!canMoveTo(next)) {
            error("global violation tier $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global violation tier"} $code"

    fun adjustedValue(): Double = round((odometer.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): GlobalViolationTier {
        return GlobalViolationTier(
            code = code,
            name = name,
            batchSize = batchSize,
            odometer = odometer,
            description = description,
            lane = lane,
            endsAt = endsAt,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global violation tier in status planned. */
fun newGlobalViolationTier(code: String, name: String): GlobalViolationTier {
    return GlobalViolationTier(
        code = code,
        name = name,
        batchSize = 0,
        odometer = 0,
        description = "",
        lane = "",
        endsAt = 0,
        currency = "",
        status = GlobalViolationTierStatus.PLANNED,
    )
}
