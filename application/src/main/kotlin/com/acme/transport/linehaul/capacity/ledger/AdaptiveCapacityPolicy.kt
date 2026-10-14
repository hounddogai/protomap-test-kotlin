package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive capacity policy is in its life cycle. */
enum class AdaptiveCapacityPolicyStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive capacity policy as the capacity team keeps it. */
data class AdaptiveCapacityPolicy(
    var code: String,
    var name: String,
    var description: String,
    var priority: Int,
    var fuelLiters: Double,
    var segment: String,
    var rate: Double,
    var status: AdaptiveCapacityPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCapacityPolicyStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCapacityPolicyStatus): Boolean {
        when (status) {
            AdaptiveCapacityPolicyStatus.DRAFT -> {
                when (next) {
                    AdaptiveCapacityPolicyStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveCapacityPolicyStatus.PENDING -> {
                when (next) {
                    AdaptiveCapacityPolicyStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveCapacityPolicyStatus.ACTIVE -> return next == AdaptiveCapacityPolicyStatus.SUSPENDED
            AdaptiveCapacityPolicyStatus.SUSPENDED -> return next == AdaptiveCapacityPolicyStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveCapacityPolicyStatus) {
        if (!canMoveTo(next)) {
            error("adaptive capacity policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveTotal(): Double = round(fuelLiters * rate * 100) / 100

    fun snapshot(): AdaptiveCapacityPolicy {
        return AdaptiveCapacityPolicy(
            code = code,
            name = name,
            description = description,
            priority = priority,
            fuelLiters = fuelLiters,
            segment = segment,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a adaptive capacity policy in status draft. */
fun newAdaptiveCapacityPolicy(code: String, name: String): AdaptiveCapacityPolicy {
    return AdaptiveCapacityPolicy(
        code = code,
        name = name,
        description = "",
        priority = 0,
        fuelLiters = 0.0,
        segment = "",
        rate = 0.0,
        status = AdaptiveCapacityPolicyStatus.DRAFT,
    )
}
