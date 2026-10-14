package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending availability preference is in its life cycle. */
enum class PendingAvailabilityPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a pending availability preference and its current status. */
data class PendingAvailabilityPreference(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var depot: String,
    var windowDays: Int,
    var segment: String,
    var source: String,
    var unit: String,
    var description: String,
    var externalRef: String,
    var score: Double,
    var status: PendingAvailabilityPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAvailabilityPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAvailabilityPreferenceStatus): Boolean {
        when (status) {
            PendingAvailabilityPreferenceStatus.REQUESTED -> {
                when (next) {
                    PendingAvailabilityPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            PendingAvailabilityPreferenceStatus.GRANTED -> return next == PendingAvailabilityPreferenceStatus.REVOKED || next == PendingAvailabilityPreferenceStatus.EXPIRED
            PendingAvailabilityPreferenceStatus.REVOKED -> {
                when (next) {
                    PendingAvailabilityPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingAvailabilityPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("pending availability preference $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"pending availability preference"} $code"

    fun netLoad(): Double = round((score - windowDays.toDouble()) * 100) / 100

    fun snapshot(): PendingAvailabilityPreference {
        return PendingAvailabilityPreference(
            code = code,
            label = label,
            updatedAt = updatedAt,
            depot = depot,
            windowDays = windowDays,
            segment = segment,
            source = source,
            unit = unit,
            description = description,
            externalRef = externalRef,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a pending availability preference in status requested. */
fun newPendingAvailabilityPreference(code: String, label: String): PendingAvailabilityPreference {
    return PendingAvailabilityPreference(
        code = code,
        label = label,
        updatedAt = 0,
        depot = "",
        windowDays = 0,
        segment = "",
        source = "",
        unit = "",
        description = "",
        externalRef = "",
        score = 0.0,
        status = PendingAvailabilityPreferenceStatus.REQUESTED,
    )
}
