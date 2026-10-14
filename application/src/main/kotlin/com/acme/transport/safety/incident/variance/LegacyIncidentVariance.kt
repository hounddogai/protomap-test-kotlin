package com.acme.transport.safety.incident.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy incident variance records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a legacy incident variance is in its life cycle. */
enum class LegacyIncidentVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a legacy incident variance and its current status. */
data class LegacyIncidentVariance(
    var code: String,
    var label: String,
    var quantity: Int,
    var pallets: Int,
    var attributes: MutableMap<String, String>,
    var title: String,
    var vehicleId: String,
    var visible: Boolean,
    var contactEmail: String,
    var score: Double,
    var status: LegacyIncidentVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyIncidentVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyIncidentVarianceStatus): Boolean {
        when (status) {
            LegacyIncidentVarianceStatus.OPEN -> return next == LegacyIncidentVarianceStatus.RESOLVED || next == LegacyIncidentVarianceStatus.ARCHIVED
            LegacyIncidentVarianceStatus.IN_REVIEW -> {
                when (next) {
                    LegacyIncidentVarianceStatus.RESOLVED, LegacyIncidentVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyIncidentVarianceStatus.RESOLVED -> return next == LegacyIncidentVarianceStatus.REOPENED || next == LegacyIncidentVarianceStatus.ARCHIVED
            LegacyIncidentVarianceStatus.REOPENED -> {
                when (next) {
                    LegacyIncidentVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyIncidentVarianceStatus) {
        if (!canMoveTo(next)) {
            error("legacy incident variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy incident variance"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round(pallets.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): LegacyIncidentVariance {
        return LegacyIncidentVariance(
            code = code,
            label = label,
            quantity = quantity,
            pallets = pallets,
            attributes = attributes,
            title = title,
            vehicleId = vehicleId,
            visible = visible,
            contactEmail = contactEmail,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy incident variance in status open. */
fun newLegacyIncidentVariance(code: String, label: String): LegacyIncidentVariance {
    return LegacyIncidentVariance(
        code = code,
        label = label,
        quantity = 0,
        pallets = 0,
        attributes = mutableMapOf<String, String>(),
        title = "",
        vehicleId = "",
        visible = false,
        contactEmail = "",
        score = 0.0,
        status = LegacyIncidentVarianceStatus.OPEN,
    )
}
