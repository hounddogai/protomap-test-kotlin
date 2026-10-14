package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization entry is in its life cycle. */
enum class UtilizationEntryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a utilization entry and its current status. */
data class UtilizationEntry(
    var code: String,
    var title: String,
    var loadKg: Double,
    var threshold: Double,
    var windowDays: Int,
    var expiresAt: Int,
    var active: Boolean,
    var sequence: Int,
    var attributes: MutableMap<String, String>,
    var status: UtilizationEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationEntryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationEntryStatus): Boolean {
        when (status) {
            UtilizationEntryStatus.PLANNED -> {
                when (next) {
                    UtilizationEntryStatus.IN_PROGRESS, UtilizationEntryStatus.COMPLETED -> return true
                }
                return false
            }
            UtilizationEntryStatus.SCHEDULED -> {
                when (next) {
                    UtilizationEntryStatus.COMPLETED -> return true
                }
                return false
            }
            UtilizationEntryStatus.IN_PROGRESS -> {
                when (next) {
                    UtilizationEntryStatus.CANCELLED -> return true
                }
                return false
            }
            UtilizationEntryStatus.COMPLETED -> return next == UtilizationEntryStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: UtilizationEntryStatus) {
        if (!canMoveTo(next)) {
            error("utilization entry $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round((threshold + sequence.toDouble()) * 100) / 100

    fun snapshot(): UtilizationEntry {
        return UtilizationEntry(
            code = code,
            title = title,
            loadKg = loadKg,
            threshold = threshold,
            windowDays = windowDays,
            expiresAt = expiresAt,
            active = active,
            sequence = sequence,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a utilization entry in status planned. */
fun newUtilizationEntry(code: String, title: String): UtilizationEntry {
    return UtilizationEntry(
        code = code,
        title = title,
        loadKg = 0.0,
        threshold = 0.0,
        windowDays = 0,
        expiresAt = 0,
        active = false,
        sequence = 0,
        attributes = mutableMapOf<String, String>(),
        status = UtilizationEntryStatus.PLANNED,
    )
}
