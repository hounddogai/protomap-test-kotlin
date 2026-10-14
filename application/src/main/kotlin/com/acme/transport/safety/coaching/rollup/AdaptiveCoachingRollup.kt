package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive coaching rollup records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a adaptive coaching rollup is in its life cycle. */
enum class AdaptiveCoachingRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One adaptive coaching rollup, with what the planners need to act on it. */
data class AdaptiveCoachingRollup(
    var code: String,
    var label: String,
    var graceDays: Int,
    var currency: String,
    var version: Int,
    var attempts: Int,
    var contactEmail: String,
    var retries: Int,
    var vehicleId: String,
    var quantity: Int,
    var status: AdaptiveCoachingRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCoachingRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCoachingRollupStatus): Boolean {
        when (status) {
            AdaptiveCoachingRollupStatus.PROPOSED -> {
                when (next) {
                    AdaptiveCoachingRollupStatus.REJECTED, AdaptiveCoachingRollupStatus.WITHDRAWN -> return true
                }
                return false
            }
            AdaptiveCoachingRollupStatus.APPROVED -> return next == AdaptiveCoachingRollupStatus.REJECTED || next == AdaptiveCoachingRollupStatus.WITHDRAWN
            AdaptiveCoachingRollupStatus.REJECTED -> return next == AdaptiveCoachingRollupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AdaptiveCoachingRollupStatus) {
        if (!canMoveTo(next)) {
            error("adaptive coaching rollup $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round(attempts.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): AdaptiveCoachingRollup {
        return AdaptiveCoachingRollup(
            code = code,
            label = label,
            graceDays = graceDays,
            currency = currency,
            version = version,
            attempts = attempts,
            contactEmail = contactEmail,
            retries = retries,
            vehicleId = vehicleId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive coaching rollup in status proposed. */
fun newAdaptiveCoachingRollup(code: String, label: String): AdaptiveCoachingRollup {
    return AdaptiveCoachingRollup(
        code = code,
        label = label,
        graceDays = 0,
        currency = "",
        version = 0,
        attempts = 0,
        contactEmail = "",
        retries = 0,
        vehicleId = "",
        quantity = 0,
        status = AdaptiveCoachingRollupStatus.PROPOSED,
    )
}
