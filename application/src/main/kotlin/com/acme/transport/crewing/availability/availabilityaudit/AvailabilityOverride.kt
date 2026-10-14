package com.acme.transport.crewing.availability.availabilityaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability override is in its life cycle. */
enum class AvailabilityOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One availability override, with what the planners need to act on it. */
data class AvailabilityOverride(
    var code: String,
    var label: String,
    var visible: Boolean,
    var endsAt: Int,
    var weight: Double,
    var updatedAt: Int,
    var category: String,
    var rate: Double,
    var budget: Double,
    var status: AvailabilityOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityOverrideStatus): Boolean {
        when (status) {
            AvailabilityOverrideStatus.REQUESTED -> return next == AvailabilityOverrideStatus.REVOKED || next == AvailabilityOverrideStatus.EXPIRED
            AvailabilityOverrideStatus.GRANTED -> {
                when (next) {
                    AvailabilityOverrideStatus.EXPIRED -> return true
                }
                return false
            }
            AvailabilityOverrideStatus.REVOKED -> {
                when (next) {
                    AvailabilityOverrideStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityOverrideStatus) {
        if (!canMoveTo(next)) {
            error("availability override $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedExposure(): Double = round((updatedAt.toDouble() + weight) * 100) / 100

    fun snapshot(): AvailabilityOverride {
        return AvailabilityOverride(
            code = code,
            label = label,
            visible = visible,
            endsAt = endsAt,
            weight = weight,
            updatedAt = updatedAt,
            category = category,
            rate = rate,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a availability override in status requested. */
fun newAvailabilityOverride(code: String, label: String): AvailabilityOverride {
    return AvailabilityOverride(
        code = code,
        label = label,
        visible = false,
        endsAt = 0,
        weight = 0.0,
        updatedAt = 0,
        category = "",
        rate = 0.0,
        budget = 0.0,
        status = AvailabilityOverrideStatus.REQUESTED,
    )
}
