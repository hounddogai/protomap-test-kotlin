package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback lane plan preference records of linehaul's lane plan work, and the rules, stores, and
// calculations around them.

/** Where a fallback lane plan preference is in its life cycle. */
enum class FallbackLanePlanPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fallback lane plan preference as the lane plan team keeps it. */
data class FallbackLanePlanPreference(
    var code: String,
    var name: String,
    var attempts: Int,
    var ratio: Double,
    var cost: Double,
    var floorValue: Double,
    var minimum: Double,
    var contactEmail: String,
    var maximum: Double,
    var version: Int,
    var category: String,
    var status: FallbackLanePlanPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackLanePlanPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackLanePlanPreferenceStatus): Boolean {
        when (status) {
            FallbackLanePlanPreferenceStatus.REQUESTED -> return next == FallbackLanePlanPreferenceStatus.REVOKED || next == FallbackLanePlanPreferenceStatus.EXPIRED
            FallbackLanePlanPreferenceStatus.GRANTED -> {
                when (next) {
                    FallbackLanePlanPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackLanePlanPreferenceStatus.REVOKED -> {
                when (next) {
                    FallbackLanePlanPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackLanePlanPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("fallback lane plan preference $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round(maximum * minimum * 100) / 100

    fun snapshot(): FallbackLanePlanPreference {
        return FallbackLanePlanPreference(
            code = code,
            name = name,
            attempts = attempts,
            ratio = ratio,
            cost = cost,
            floorValue = floorValue,
            minimum = minimum,
            contactEmail = contactEmail,
            maximum = maximum,
            version = version,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fallback lane plan preference in status requested. */
fun newFallbackLanePlanPreference(code: String, name: String): FallbackLanePlanPreference {
    return FallbackLanePlanPreference(
        code = code,
        name = name,
        attempts = 0,
        ratio = 0.0,
        cost = 0.0,
        floorValue = 0.0,
        minimum = 0.0,
        contactEmail = "",
        maximum = 0.0,
        version = 0,
        category = "",
        status = FallbackLanePlanPreferenceStatus.REQUESTED,
    )
}
