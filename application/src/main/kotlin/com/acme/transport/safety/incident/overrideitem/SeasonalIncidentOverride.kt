package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal incident override records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a seasonal incident override is in its life cycle. */
enum class SeasonalIncidentOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal incident override. Its code is unique within a tenant. */
data class SeasonalIncidentOverride(
    var code: String,
    var title: String,
    var timezone: String,
    var country: String,
    var shiftCode: String,
    var threshold: Double,
    var pallets: Int,
    var etaMinutes: Int,
    var tags: MutableList<String>,
    var status: SeasonalIncidentOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalIncidentOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalIncidentOverrideStatus): Boolean {
        when (status) {
            SeasonalIncidentOverrideStatus.DRAFT -> {
                when (next) {
                    SeasonalIncidentOverrideStatus.ACTIVE, SeasonalIncidentOverrideStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalIncidentOverrideStatus.PENDING -> return next == SeasonalIncidentOverrideStatus.SUSPENDED || next == SeasonalIncidentOverrideStatus.CLOSED
            SeasonalIncidentOverrideStatus.ACTIVE -> return next == SeasonalIncidentOverrideStatus.SUSPENDED
            SeasonalIncidentOverrideStatus.SUSPENDED -> {
                when (next) {
                    SeasonalIncidentOverrideStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalIncidentOverrideStatus) {
        if (!canMoveTo(next)) {
            error("seasonal incident override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal incident override"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round((pallets.toDouble() + threshold) * 100) / 100

    fun snapshot(): SeasonalIncidentOverride {
        return SeasonalIncidentOverride(
            code = code,
            title = title,
            timezone = timezone,
            country = country,
            shiftCode = shiftCode,
            threshold = threshold,
            pallets = pallets,
            etaMinutes = etaMinutes,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a seasonal incident override in status draft. */
fun newSeasonalIncidentOverride(code: String, title: String): SeasonalIncidentOverride {
    return SeasonalIncidentOverride(
        code = code,
        title = title,
        timezone = "",
        country = "",
        shiftCode = "",
        threshold = 0.0,
        pallets = 0,
        etaMinutes = 0,
        tags = mutableListOf<String>(),
        status = SeasonalIncidentOverrideStatus.DRAFT,
    )
}
