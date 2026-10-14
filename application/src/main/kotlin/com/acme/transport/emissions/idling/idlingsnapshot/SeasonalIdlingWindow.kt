package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal idling window is in its life cycle. */
enum class SeasonalIdlingWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal idling window as the idling team keeps it. */
data class SeasonalIdlingWindow(
    var code: String,
    var label: String,
    var locale: String,
    var attributes: MutableMap<String, String>,
    var category: String,
    var notes: String,
    var segment: String,
    var reviewer: String,
    var fuelLiters: Double,
    var attempts: Int,
    var tags: MutableList<String>,
    var status: SeasonalIdlingWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalIdlingWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalIdlingWindowStatus): Boolean {
        when (status) {
            SeasonalIdlingWindowStatus.OPEN -> {
                when (next) {
                    SeasonalIdlingWindowStatus.IN_REVIEW -> return true
                }
                return false
            }
            SeasonalIdlingWindowStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalIdlingWindowStatus.RESOLVED -> return true
                }
                return false
            }
            SeasonalIdlingWindowStatus.RESOLVED -> {
                when (next) {
                    SeasonalIdlingWindowStatus.REOPENED -> return true
                }
                return false
            }
            SeasonalIdlingWindowStatus.REOPENED -> {
                when (next) {
                    SeasonalIdlingWindowStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalIdlingWindowStatus) {
        if (!canMoveTo(next)) {
            error("seasonal idling window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round((fuelLiters - attempts.toDouble()) * 100) / 100

    fun snapshot(): SeasonalIdlingWindow {
        return SeasonalIdlingWindow(
            code = code,
            label = label,
            locale = locale,
            attributes = attributes,
            category = category,
            notes = notes,
            segment = segment,
            reviewer = reviewer,
            fuelLiters = fuelLiters,
            attempts = attempts,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a seasonal idling window in status open. */
fun newSeasonalIdlingWindow(code: String, label: String): SeasonalIdlingWindow {
    return SeasonalIdlingWindow(
        code = code,
        label = label,
        locale = "",
        attributes = mutableMapOf<String, String>(),
        category = "",
        notes = "",
        segment = "",
        reviewer = "",
        fuelLiters = 0.0,
        attempts = 0,
        tags = mutableListOf<String>(),
        status = SeasonalIdlingWindowStatus.OPEN,
    )
}
