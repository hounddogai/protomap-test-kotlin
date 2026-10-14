package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback backhaul preference is in its life cycle. */
enum class FallbackBackhaulPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a fallback backhaul preference and its current status. */
data class FallbackBackhaulPreference(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var weight: Double,
    var timezone: String,
    var sequence: Int,
    var fuelLiters: Double,
    var contactEmail: String,
    var budget: Double,
    var maximum: Double,
    var status: FallbackBackhaulPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackBackhaulPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackBackhaulPreferenceStatus): Boolean {
        when (status) {
            FallbackBackhaulPreferenceStatus.NEW -> {
                when (next) {
                    FallbackBackhaulPreferenceStatus.RUNNING -> return true
                }
                return false
            }
            FallbackBackhaulPreferenceStatus.QUEUED -> return next == FallbackBackhaulPreferenceStatus.DONE
            FallbackBackhaulPreferenceStatus.RUNNING -> {
                when (next) {
                    FallbackBackhaulPreferenceStatus.DONE, FallbackBackhaulPreferenceStatus.FAILED -> return true
                }
                return false
            }
            FallbackBackhaulPreferenceStatus.DONE -> {
                when (next) {
                    FallbackBackhaulPreferenceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackBackhaulPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("fallback backhaul preference $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback backhaul preference"} $code"

    fun projectedLoad(): Double = round(maximum * budget * 100) / 100

    fun snapshot(): FallbackBackhaulPreference {
        return FallbackBackhaulPreference(
            code = code,
            name = name,
            expiresAt = expiresAt,
            weight = weight,
            timezone = timezone,
            sequence = sequence,
            fuelLiters = fuelLiters,
            contactEmail = contactEmail,
            budget = budget,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback backhaul preference in status new. */
fun newFallbackBackhaulPreference(code: String, name: String): FallbackBackhaulPreference {
    return FallbackBackhaulPreference(
        code = code,
        name = name,
        expiresAt = 0,
        weight = 0.0,
        timezone = "",
        sequence = 0,
        fuelLiters = 0.0,
        contactEmail = "",
        budget = 0.0,
        maximum = 0.0,
        status = FallbackBackhaulPreferenceStatus.NEW,
    )
}
