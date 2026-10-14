package com.acme.transport.safety.nearmiss.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary near miss policy is in its life cycle. */
enum class PrimaryNearMissPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A primary near miss policy as the near miss team keeps it. */
data class PrimaryNearMissPolicy(
    var code: String,
    var name: String,
    var ceiling: Double,
    var currency: String,
    var maximum: Double,
    var startsAt: Int,
    var status: PrimaryNearMissPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryNearMissPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryNearMissPolicyStatus): Boolean {
        when (status) {
            PrimaryNearMissPolicyStatus.PLANNED -> {
                when (next) {
                    PrimaryNearMissPolicyStatus.SCHEDULED -> return true
                }
                return false
            }
            PrimaryNearMissPolicyStatus.SCHEDULED -> {
                when (next) {
                    PrimaryNearMissPolicyStatus.IN_PROGRESS, PrimaryNearMissPolicyStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryNearMissPolicyStatus.IN_PROGRESS -> {
                when (next) {
                    PrimaryNearMissPolicyStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryNearMissPolicyStatus.COMPLETED -> {
                when (next) {
                    PrimaryNearMissPolicyStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryNearMissPolicyStatus) {
        if (!canMoveTo(next)) {
            error("primary near miss policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedTotal(): Double = round((maximum - startsAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryNearMissPolicy {
        return PrimaryNearMissPolicy(
            code = code,
            name = name,
            ceiling = ceiling,
            currency = currency,
            maximum = maximum,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary near miss policy in status planned. */
fun newPrimaryNearMissPolicy(code: String, name: String): PrimaryNearMissPolicy {
    return PrimaryNearMissPolicy(
        code = code,
        name = name,
        ceiling = 0.0,
        currency = "",
        maximum = 0.0,
        startsAt = 0,
        status = PrimaryNearMissPolicyStatus.PLANNED,
    )
}
