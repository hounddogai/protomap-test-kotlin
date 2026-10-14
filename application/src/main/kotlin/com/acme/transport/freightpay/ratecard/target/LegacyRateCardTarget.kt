package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy rate card target records of freightpay's rate card work, and the rules, stores, and calculations
// around them.

/** Where a legacy rate card target is in its life cycle. */
enum class LegacyRateCardTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy rate card target. Its code is unique within a tenant. */
data class LegacyRateCardTarget(
    var code: String,
    var label: String,
    var ratio: Double,
    var minimum: Double,
    var etaMinutes: Int,
    var fuelLiters: Double,
    var status: LegacyRateCardTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRateCardTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRateCardTargetStatus): Boolean {
        when (status) {
            LegacyRateCardTargetStatus.IDLE -> {
                when (next) {
                    LegacyRateCardTargetStatus.WARMING -> return true
                }
                return false
            }
            LegacyRateCardTargetStatus.WARMING -> return next == LegacyRateCardTargetStatus.READY || next == LegacyRateCardTargetStatus.STOPPED
            LegacyRateCardTargetStatus.READY -> return next == LegacyRateCardTargetStatus.DRAINING
            LegacyRateCardTargetStatus.DRAINING -> {
                when (next) {
                    LegacyRateCardTargetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyRateCardTargetStatus) {
        if (!canMoveTo(next)) {
            error("legacy rate card target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy rate card target"} $code"

    fun adjustedTotal(): Double = round((ratio + minimum) * 100) / 100

    fun snapshot(): LegacyRateCardTarget {
        return LegacyRateCardTarget(
            code = code,
            label = label,
            ratio = ratio,
            minimum = minimum,
            etaMinutes = etaMinutes,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy rate card target in status idle. */
fun newLegacyRateCardTarget(code: String, label: String): LegacyRateCardTarget {
    return LegacyRateCardTarget(
        code = code,
        label = label,
        ratio = 0.0,
        minimum = 0.0,
        etaMinutes = 0,
        fuelLiters = 0.0,
        status = LegacyRateCardTargetStatus.IDLE,
    )
}
