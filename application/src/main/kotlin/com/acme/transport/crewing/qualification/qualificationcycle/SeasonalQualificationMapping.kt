package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal qualification mapping is in its life cycle. */
enum class SeasonalQualificationMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a seasonal qualification mapping and its current status. */
data class SeasonalQualificationMapping(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var label: String,
    var stopCount: Int,
    var source: String,
    var maximum: Double,
    var trailerId: String,
    var segment: String,
    var capacity: Int,
    var status: SeasonalQualificationMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalQualificationMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalQualificationMappingStatus): Boolean {
        when (status) {
            SeasonalQualificationMappingStatus.OPEN -> return next == SeasonalQualificationMappingStatus.IN_REVIEW || next == SeasonalQualificationMappingStatus.REOPENED
            SeasonalQualificationMappingStatus.IN_REVIEW -> return next == SeasonalQualificationMappingStatus.RESOLVED || next == SeasonalQualificationMappingStatus.REOPENED
            SeasonalQualificationMappingStatus.RESOLVED -> {
                when (next) {
                    SeasonalQualificationMappingStatus.REOPENED, SeasonalQualificationMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalQualificationMappingStatus.REOPENED -> return next == SeasonalQualificationMappingStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalQualificationMappingStatus) {
        if (!canMoveTo(next)) {
            error("seasonal qualification mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round((stopCount.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): SeasonalQualificationMapping {
        return SeasonalQualificationMapping(
            code = code,
            title = title,
            attributes = attributes,
            label = label,
            stopCount = stopCount,
            source = source,
            maximum = maximum,
            trailerId = trailerId,
            segment = segment,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal qualification mapping in status open. */
fun newSeasonalQualificationMapping(code: String, title: String): SeasonalQualificationMapping {
    return SeasonalQualificationMapping(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        label = "",
        stopCount = 0,
        source = "",
        maximum = 0.0,
        trailerId = "",
        segment = "",
        capacity = 0,
        status = SeasonalQualificationMappingStatus.OPEN,
    )
}
