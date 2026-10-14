package com.acme.transport.safety.coaching.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional coaching mapping is in its life cycle. */
enum class RegionalCoachingMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional coaching mapping and its current status. */
data class RegionalCoachingMapping(
    var code: String,
    var title: String,
    var graceDays: Int,
    var minimum: Double,
    var carrier: String,
    var timezone: String,
    var country: String,
    var description: String,
    var threshold: Double,
    var status: RegionalCoachingMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCoachingMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCoachingMappingStatus): Boolean {
        when (status) {
            RegionalCoachingMappingStatus.CREATED -> return next == RegionalCoachingMappingStatus.PUBLISHED
            RegionalCoachingMappingStatus.VALIDATED -> return next == RegionalCoachingMappingStatus.PUBLISHED || next == RegionalCoachingMappingStatus.RETIRED
            RegionalCoachingMappingStatus.PUBLISHED -> {
                when (next) {
                    RegionalCoachingMappingStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCoachingMappingStatus) {
        if (!canMoveTo(next)) {
            error("regional coaching mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional coaching mapping"} $code"

    fun netTotal(): Double = round((minimum + graceDays.toDouble()) * 100) / 100

    fun snapshot(): RegionalCoachingMapping {
        return RegionalCoachingMapping(
            code = code,
            title = title,
            graceDays = graceDays,
            minimum = minimum,
            carrier = carrier,
            timezone = timezone,
            country = country,
            description = description,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a regional coaching mapping in status created. */
fun newRegionalCoachingMapping(code: String, title: String): RegionalCoachingMapping {
    return RegionalCoachingMapping(
        code = code,
        title = title,
        graceDays = 0,
        minimum = 0.0,
        carrier = "",
        timezone = "",
        country = "",
        description = "",
        threshold = 0.0,
        status = RegionalCoachingMappingStatus.CREATED,
    )
}
