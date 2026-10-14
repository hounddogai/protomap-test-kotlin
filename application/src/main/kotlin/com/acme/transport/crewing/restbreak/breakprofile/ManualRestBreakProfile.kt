package com.acme.transport.crewing.restbreak.breakprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual rest break profile records of crewing's rest break work, and the rules, stores, and calculations
// around them.

/** Where a manual rest break profile is in its life cycle. */
enum class ManualRestBreakProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One manual rest break profile, with what the planners need to act on it. */
data class ManualRestBreakProfile(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var attempts: Int,
    var stopCount: Int,
    var phone: String,
    var status: ManualRestBreakProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRestBreakProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRestBreakProfileStatus): Boolean {
        when (status) {
            ManualRestBreakProfileStatus.PLANNED -> return next == ManualRestBreakProfileStatus.CANCELLED
            ManualRestBreakProfileStatus.SCHEDULED -> {
                when (next) {
                    ManualRestBreakProfileStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ManualRestBreakProfileStatus.IN_PROGRESS -> return next == ManualRestBreakProfileStatus.COMPLETED || next == ManualRestBreakProfileStatus.CANCELLED
            ManualRestBreakProfileStatus.COMPLETED -> return next == ManualRestBreakProfileStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ManualRestBreakProfileStatus) {
        if (!canMoveTo(next)) {
            error("manual rest break profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedExposure(): Double = round((attempts.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): ManualRestBreakProfile {
        return ManualRestBreakProfile(
            code = code,
            label = label,
            attributes = attributes,
            attempts = attempts,
            stopCount = stopCount,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual rest break profile in status planned. */
fun newManualRestBreakProfile(code: String, label: String): ManualRestBreakProfile {
    return ManualRestBreakProfile(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        attempts = 0,
        stopCount = 0,
        phone = "",
        status = ManualRestBreakProfileStatus.PLANNED,
    )
}
