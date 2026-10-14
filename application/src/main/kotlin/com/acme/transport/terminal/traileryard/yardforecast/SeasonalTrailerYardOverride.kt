package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal trailer yard override is in its life cycle. */
enum class SeasonalTrailerYardOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal trailer yard override. Its code is unique within a tenant. */
data class SeasonalTrailerYardOverride(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var channel: String,
    var etaMinutes: Int,
    var version: Int,
    var description: String,
    var ceiling: Double,
    var visible: Boolean,
    var notes: String,
    var status: SeasonalTrailerYardOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTrailerYardOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTrailerYardOverrideStatus): Boolean {
        when (status) {
            SeasonalTrailerYardOverrideStatus.REQUESTED -> return next == SeasonalTrailerYardOverrideStatus.REVOKED
            SeasonalTrailerYardOverrideStatus.GRANTED -> {
                when (next) {
                    SeasonalTrailerYardOverrideStatus.REVOKED, SeasonalTrailerYardOverrideStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalTrailerYardOverrideStatus.REVOKED -> {
                when (next) {
                    SeasonalTrailerYardOverrideStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTrailerYardOverrideStatus) {
        if (!canMoveTo(next)) {
            error("seasonal trailer yard override $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round(etaMinutes.toDouble() * ceiling * 100) / 100

    fun snapshot(): SeasonalTrailerYardOverride {
        return SeasonalTrailerYardOverride(
            code = code,
            name = name,
            attributes = attributes,
            channel = channel,
            etaMinutes = etaMinutes,
            version = version,
            description = description,
            ceiling = ceiling,
            visible = visible,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a seasonal trailer yard override in status requested. */
fun newSeasonalTrailerYardOverride(code: String, name: String): SeasonalTrailerYardOverride {
    return SeasonalTrailerYardOverride(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        channel = "",
        etaMinutes = 0,
        version = 0,
        description = "",
        ceiling = 0.0,
        visible = false,
        notes = "",
        status = SeasonalTrailerYardOverrideStatus.REQUESTED,
    )
}
