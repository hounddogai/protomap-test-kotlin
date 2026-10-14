package com.acme.transport.terminal.dockdoor.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal dock door tier is in its life cycle. */
enum class SeasonalDockDoorTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A seasonal dock door tier as the dock door team keeps it. */
data class SeasonalDockDoorTier(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var retries: Int,
    var minimum: Double,
    var currency: String,
    var priority: Int,
    var budget: Double,
    var summary: String,
    var status: SeasonalDockDoorTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDockDoorTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDockDoorTierStatus): Boolean {
        when (status) {
            SeasonalDockDoorTierStatus.PLANNED -> return next == SeasonalDockDoorTierStatus.CANCELLED
            SeasonalDockDoorTierStatus.SCHEDULED -> {
                when (next) {
                    SeasonalDockDoorTierStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalDockDoorTierStatus.IN_PROGRESS -> return next == SeasonalDockDoorTierStatus.COMPLETED || next == SeasonalDockDoorTierStatus.CANCELLED
            SeasonalDockDoorTierStatus.COMPLETED -> {
                when (next) {
                    SeasonalDockDoorTierStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDockDoorTierStatus) {
        if (!canMoveTo(next)) {
            error("seasonal dock door tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((priority.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): SeasonalDockDoorTier {
        return SeasonalDockDoorTier(
            code = code,
            name = name,
            attributes = attributes,
            retries = retries,
            minimum = minimum,
            currency = currency,
            priority = priority,
            budget = budget,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal dock door tier in status planned. */
fun newSeasonalDockDoorTier(code: String, name: String): SeasonalDockDoorTier {
    return SeasonalDockDoorTier(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        retries = 0,
        minimum = 0.0,
        currency = "",
        priority = 0,
        budget = 0.0,
        summary = "",
        status = SeasonalDockDoorTierStatus.PLANNED,
    )
}
