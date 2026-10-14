package com.acme.transport.customs.hold.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive hold target is in its life cycle. */
enum class AdaptiveHoldTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive hold target. Its code is unique within a tenant. */
data class AdaptiveHoldTarget(
    var code: String,
    var name: String,
    var unit: String,
    var distanceKm: Double,
    var active: Boolean,
    var attempts: Int,
    var channel: String,
    var status: AdaptiveHoldTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveHoldTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveHoldTargetStatus): Boolean {
        when (status) {
            AdaptiveHoldTargetStatus.DRAFT -> return next == AdaptiveHoldTargetStatus.PENDING
            AdaptiveHoldTargetStatus.PENDING -> {
                when (next) {
                    AdaptiveHoldTargetStatus.ACTIVE, AdaptiveHoldTargetStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveHoldTargetStatus.ACTIVE -> return next == AdaptiveHoldTargetStatus.CLOSED
            AdaptiveHoldTargetStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveHoldTargetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveHoldTargetStatus) {
        if (!canMoveTo(next)) {
            error("adaptive hold target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedTotal(): Double = round(attempts.toDouble() * distanceKm * 100) / 100

    fun snapshot(): AdaptiveHoldTarget {
        return AdaptiveHoldTarget(
            code = code,
            name = name,
            unit = unit,
            distanceKm = distanceKm,
            active = active,
            attempts = attempts,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive hold target in status draft. */
fun newAdaptiveHoldTarget(code: String, name: String): AdaptiveHoldTarget {
    return AdaptiveHoldTarget(
        code = code,
        name = name,
        unit = "",
        distanceKm = 0.0,
        active = false,
        attempts = 0,
        channel = "",
        status = AdaptiveHoldTargetStatus.DRAFT,
    )
}
