package com.acme.transport.lastmile.redelivery.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional redelivery claim records of lastmile's redelivery work, and the rules, stores, and
// calculations around them.

/** Where a regional redelivery claim is in its life cycle. */
enum class RegionalRedeliveryClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One regional redelivery claim, with what the planners need to act on it. */
data class RegionalRedeliveryClaim(
    var code: String,
    var label: String,
    var maximum: Double,
    var graceDays: Int,
    var carrier: String,
    var timezone: String,
    var status: RegionalRedeliveryClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRedeliveryClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRedeliveryClaimStatus): Boolean {
        when (status) {
            RegionalRedeliveryClaimStatus.NEW -> {
                when (next) {
                    RegionalRedeliveryClaimStatus.RUNNING, RegionalRedeliveryClaimStatus.DONE -> return true
                }
                return false
            }
            RegionalRedeliveryClaimStatus.QUEUED -> {
                when (next) {
                    RegionalRedeliveryClaimStatus.FAILED -> return true
                }
                return false
            }
            RegionalRedeliveryClaimStatus.RUNNING -> {
                when (next) {
                    RegionalRedeliveryClaimStatus.DONE, RegionalRedeliveryClaimStatus.FAILED -> return true
                }
                return false
            }
            RegionalRedeliveryClaimStatus.DONE -> return next == RegionalRedeliveryClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalRedeliveryClaimStatus) {
        if (!canMoveTo(next)) {
            error("regional redelivery claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional redelivery claim"} $code"

    fun adjustedExposure(): Double = round((graceDays.toDouble() + maximum) * 100) / 100

    fun snapshot(): RegionalRedeliveryClaim {
        return RegionalRedeliveryClaim(
            code = code,
            label = label,
            maximum = maximum,
            graceDays = graceDays,
            carrier = carrier,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional redelivery claim in status new. */
fun newRegionalRedeliveryClaim(code: String, label: String): RegionalRedeliveryClaim {
    return RegionalRedeliveryClaim(
        code = code,
        label = label,
        maximum = 0.0,
        graceDays = 0,
        carrier = "",
        timezone = "",
        status = RegionalRedeliveryClaimStatus.NEW,
    )
}
