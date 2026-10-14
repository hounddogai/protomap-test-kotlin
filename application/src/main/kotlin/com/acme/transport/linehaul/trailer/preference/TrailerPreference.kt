package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer preference records of linehaul's trailer work, and the rules, stores, and calculations around
// them.

/** Where a trailer preference is in its life cycle. */
enum class TrailerPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A trailer preference. Its code is unique within a tenant. */
data class TrailerPreference(
    var code: String,
    var label: String,
    var graceDays: Int,
    var fuelLiters: Double,
    var approved: Boolean,
    var limit: Int,
    var reviewer: String,
    var currency: String,
    var description: String,
    var routeCode: String,
    var status: TrailerPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerPreferenceStatus): Boolean {
        when (status) {
            TrailerPreferenceStatus.PLANNED -> return next == TrailerPreferenceStatus.COMPLETED
            TrailerPreferenceStatus.SCHEDULED -> return next == TrailerPreferenceStatus.COMPLETED
            TrailerPreferenceStatus.IN_PROGRESS -> {
                when (next) {
                    TrailerPreferenceStatus.COMPLETED, TrailerPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
            TrailerPreferenceStatus.COMPLETED -> return next == TrailerPreferenceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TrailerPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("trailer preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round((fuelLiters + limit.toDouble()) * 100) / 100

    fun snapshot(): TrailerPreference {
        return TrailerPreference(
            code = code,
            label = label,
            graceDays = graceDays,
            fuelLiters = fuelLiters,
            approved = approved,
            limit = limit,
            reviewer = reviewer,
            currency = currency,
            description = description,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer preference in status planned. */
fun newTrailerPreference(code: String, label: String): TrailerPreference {
    return TrailerPreference(
        code = code,
        label = label,
        graceDays = 0,
        fuelLiters = 0.0,
        approved = false,
        limit = 0,
        reviewer = "",
        currency = "",
        description = "",
        routeCode = "",
        status = TrailerPreferenceStatus.PLANNED,
    )
}
