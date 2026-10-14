package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The roster variance records of crewing's roster work, and the rules, stores, and calculations around them.

/** Where a roster variance is in its life cycle. */
enum class RosterVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a roster variance and its current status. */
data class RosterVariance(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var threshold: Double,
    var graceDays: Int,
    var endsAt: Int,
    var externalRef: String,
    var reviewer: String,
    var driverId: String,
    var ratio: Double,
    var channel: String,
    var status: RosterVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterVarianceStatus): Boolean {
        when (status) {
            RosterVarianceStatus.PLANNED -> return next == RosterVarianceStatus.COMPLETED || next == RosterVarianceStatus.CANCELLED
            RosterVarianceStatus.SCHEDULED -> return next == RosterVarianceStatus.COMPLETED
            RosterVarianceStatus.IN_PROGRESS -> return next == RosterVarianceStatus.COMPLETED || next == RosterVarianceStatus.CANCELLED
            RosterVarianceStatus.COMPLETED -> return next == RosterVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RosterVarianceStatus) {
        if (!canMoveTo(next)) {
            error("roster variance $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round(threshold * endsAt.toDouble() * 100) / 100

    fun snapshot(): RosterVariance {
        return RosterVariance(
            code = code,
            label = label,
            attributes = attributes,
            threshold = threshold,
            graceDays = graceDays,
            endsAt = endsAt,
            externalRef = externalRef,
            reviewer = reviewer,
            driverId = driverId,
            ratio = ratio,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a roster variance in status planned. */
fun newRosterVariance(code: String, label: String): RosterVariance {
    return RosterVariance(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        threshold = 0.0,
        graceDays = 0,
        endsAt = 0,
        externalRef = "",
        reviewer = "",
        driverId = "",
        ratio = 0.0,
        channel = "",
        status = RosterVarianceStatus.PLANNED,
    )
}
