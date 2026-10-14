package com.acme.transport.crewing.availability.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal availability template is in its life cycle. */
enum class SeasonalAvailabilityTemplateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One seasonal availability template, with what the planners need to act on it. */
data class SeasonalAvailabilityTemplate(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var segment: String,
    var revision: Int,
    var ceiling: Double,
    var weight: Double,
    var etaMinutes: Int,
    var status: SeasonalAvailabilityTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAvailabilityTemplateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAvailabilityTemplateStatus): Boolean {
        when (status) {
            SeasonalAvailabilityTemplateStatus.REQUESTED -> {
                when (next) {
                    SeasonalAvailabilityTemplateStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalAvailabilityTemplateStatus.GRANTED -> return next == SeasonalAvailabilityTemplateStatus.REVOKED
            SeasonalAvailabilityTemplateStatus.REVOKED -> {
                when (next) {
                    SeasonalAvailabilityTemplateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalAvailabilityTemplateStatus) {
        if (!canMoveTo(next)) {
            error("seasonal availability template $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round(etaMinutes.toDouble() * ceiling * 100) / 100

    fun snapshot(): SeasonalAvailabilityTemplate {
        return SeasonalAvailabilityTemplate(
            code = code,
            name = name,
            attributes = attributes,
            segment = segment,
            revision = revision,
            ceiling = ceiling,
            weight = weight,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a seasonal availability template in status requested. */
fun newSeasonalAvailabilityTemplate(code: String, name: String): SeasonalAvailabilityTemplate {
    return SeasonalAvailabilityTemplate(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        segment = "",
        revision = 0,
        ceiling = 0.0,
        weight = 0.0,
        etaMinutes = 0,
        status = SeasonalAvailabilityTemplateStatus.REQUESTED,
    )
}
