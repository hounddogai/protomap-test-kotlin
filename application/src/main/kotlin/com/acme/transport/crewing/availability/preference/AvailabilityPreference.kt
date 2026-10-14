package com.acme.transport.crewing.availability.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability preference records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability preference is in its life cycle. */
enum class AvailabilityPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One availability preference, with what the planners need to act on it. */
data class AvailabilityPreference(
    var code: String,
    var title: String,
    var threshold: Double,
    var limit: Int,
    var ownerId: String,
    var externalRef: String,
    var dock: String,
    var status: AvailabilityPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityPreferenceStatus): Boolean {
        when (status) {
            AvailabilityPreferenceStatus.PLANNED -> {
                when (next) {
                    AvailabilityPreferenceStatus.SCHEDULED -> return true
                }
                return false
            }
            AvailabilityPreferenceStatus.SCHEDULED -> return next == AvailabilityPreferenceStatus.COMPLETED || next == AvailabilityPreferenceStatus.CANCELLED
            AvailabilityPreferenceStatus.IN_PROGRESS -> {
                when (next) {
                    AvailabilityPreferenceStatus.COMPLETED, AvailabilityPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
            AvailabilityPreferenceStatus.COMPLETED -> {
                when (next) {
                    AvailabilityPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("availability preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"availability preference"} $code"

    fun adjustedVolume(): Double = round(threshold * limit.toDouble() * 100) / 100

    fun snapshot(): AvailabilityPreference {
        return AvailabilityPreference(
            code = code,
            title = title,
            threshold = threshold,
            limit = limit,
            ownerId = ownerId,
            externalRef = externalRef,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a availability preference in status planned. */
fun newAvailabilityPreference(code: String, title: String): AvailabilityPreference {
    return AvailabilityPreference(
        code = code,
        title = title,
        threshold = 0.0,
        limit = 0,
        ownerId = "",
        externalRef = "",
        dock = "",
        status = AvailabilityPreferenceStatus.PLANNED,
    )
}
