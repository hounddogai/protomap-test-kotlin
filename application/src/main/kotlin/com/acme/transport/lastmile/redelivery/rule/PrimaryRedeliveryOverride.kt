package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary redelivery override is in its life cycle. */
enum class PrimaryRedeliveryOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a primary redelivery override and its current status. */
data class PrimaryRedeliveryOverride(
    var code: String,
    var label: String,
    var lane: String,
    var priority: Int,
    var depot: String,
    var attempts: Int,
    var status: PrimaryRedeliveryOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRedeliveryOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRedeliveryOverrideStatus): Boolean {
        when (status) {
            PrimaryRedeliveryOverrideStatus.DRAFT -> return next == PrimaryRedeliveryOverrideStatus.PENDING || next == PrimaryRedeliveryOverrideStatus.CLOSED
            PrimaryRedeliveryOverrideStatus.PENDING -> return next == PrimaryRedeliveryOverrideStatus.CLOSED
            PrimaryRedeliveryOverrideStatus.ACTIVE -> {
                when (next) {
                    PrimaryRedeliveryOverrideStatus.SUSPENDED -> return true
                }
                return false
            }
            PrimaryRedeliveryOverrideStatus.SUSPENDED -> {
                when (next) {
                    PrimaryRedeliveryOverrideStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryRedeliveryOverrideStatus) {
        if (!canMoveTo(next)) {
            error("primary redelivery override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedValue(): Double = round((priority.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): PrimaryRedeliveryOverride {
        return PrimaryRedeliveryOverride(
            code = code,
            label = label,
            lane = lane,
            priority = priority,
            depot = depot,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a primary redelivery override in status draft. */
fun newPrimaryRedeliveryOverride(code: String, label: String): PrimaryRedeliveryOverride {
    return PrimaryRedeliveryOverride(
        code = code,
        label = label,
        lane = "",
        priority = 0,
        depot = "",
        attempts = 0,
        status = PrimaryRedeliveryOverrideStatus.DRAFT,
    )
}
