package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute preference is in its life cycle. */
enum class DisputePreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A dispute preference. Its code is unique within a tenant. */
data class DisputePreference(
    var code: String,
    var label: String,
    var title: String,
    var distanceKm: Double,
    var attributes: MutableMap<String, String>,
    var summary: String,
    var updatedAt: Int,
    var status: DisputePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputePreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputePreferenceStatus): Boolean {
        when (status) {
            DisputePreferenceStatus.PLANNED -> return next == DisputePreferenceStatus.COMPLETED
            DisputePreferenceStatus.SCHEDULED -> {
                when (next) {
                    DisputePreferenceStatus.IN_PROGRESS, DisputePreferenceStatus.COMPLETED -> return true
                }
                return false
            }
            DisputePreferenceStatus.IN_PROGRESS -> return next == DisputePreferenceStatus.COMPLETED
            DisputePreferenceStatus.COMPLETED -> return next == DisputePreferenceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DisputePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("dispute preference $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round((updatedAt.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): DisputePreference {
        return DisputePreference(
            code = code,
            label = label,
            title = title,
            distanceKm = distanceKm,
            attributes = attributes,
            summary = summary,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dispute preference in status planned. */
fun newDisputePreference(code: String, label: String): DisputePreference {
    return DisputePreference(
        code = code,
        label = label,
        title = "",
        distanceKm = 0.0,
        attributes = mutableMapOf<String, String>(),
        summary = "",
        updatedAt = 0,
        status = DisputePreferenceStatus.PLANNED,
    )
}
