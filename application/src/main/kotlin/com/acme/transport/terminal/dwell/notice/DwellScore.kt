package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell score is in its life cycle. */
enum class DwellScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a dwell score and its current status. */
data class DwellScore(
    var code: String,
    var label: String,
    var priority: Int,
    var loadKg: Double,
    var attributes: MutableMap<String, String>,
    var status: DwellScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellScoreStatus): Boolean {
        when (status) {
            DwellScoreStatus.PLANNED -> return next == DwellScoreStatus.COMPLETED || next == DwellScoreStatus.CANCELLED
            DwellScoreStatus.SCHEDULED -> return next == DwellScoreStatus.IN_PROGRESS
            DwellScoreStatus.IN_PROGRESS -> return next == DwellScoreStatus.COMPLETED || next == DwellScoreStatus.CANCELLED
            DwellScoreStatus.COMPLETED -> return next == DwellScoreStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DwellScoreStatus) {
        if (!canMoveTo(next)) {
            error("dwell score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round((priority.toDouble() - loadKg) * 100) / 100

    fun snapshot(): DwellScore {
        return DwellScore(
            code = code,
            label = label,
            priority = priority,
            loadKg = loadKg,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dwell score in status planned. */
fun newDwellScore(code: String, label: String): DwellScore {
    return DwellScore(
        code = code,
        label = label,
        priority = 0,
        loadKg = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = DwellScoreStatus.PLANNED,
    )
}
