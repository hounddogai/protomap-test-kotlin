package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict backhaul override is in its life cycle. */
enum class StrictBackhaulOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a strict backhaul override and its current status. */
data class StrictBackhaulOverride(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var source: String,
    var weight: Double,
    var graceDays: Int,
    var trailerId: String,
    var phone: String,
    var locale: String,
    var status: StrictBackhaulOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictBackhaulOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictBackhaulOverrideStatus): Boolean {
        when (status) {
            StrictBackhaulOverrideStatus.OPEN -> return next == StrictBackhaulOverrideStatus.REOPENED || next == StrictBackhaulOverrideStatus.ARCHIVED
            StrictBackhaulOverrideStatus.IN_REVIEW -> {
                when (next) {
                    StrictBackhaulOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictBackhaulOverrideStatus.RESOLVED -> return next == StrictBackhaulOverrideStatus.REOPENED
            StrictBackhaulOverrideStatus.REOPENED -> {
                when (next) {
                    StrictBackhaulOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictBackhaulOverrideStatus) {
        if (!canMoveTo(next)) {
            error("strict backhaul override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedLoad(): Double = round(weight * graceDays.toDouble() * 100) / 100

    fun snapshot(): StrictBackhaulOverride {
        return StrictBackhaulOverride(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            source = source,
            weight = weight,
            graceDays = graceDays,
            trailerId = trailerId,
            phone = phone,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a strict backhaul override in status open. */
fun newStrictBackhaulOverride(code: String, title: String): StrictBackhaulOverride {
    return StrictBackhaulOverride(
        code = code,
        title = title,
        fuelLiters = 0.0,
        source = "",
        weight = 0.0,
        graceDays = 0,
        trailerId = "",
        phone = "",
        locale = "",
        status = StrictBackhaulOverrideStatus.OPEN,
    )
}
