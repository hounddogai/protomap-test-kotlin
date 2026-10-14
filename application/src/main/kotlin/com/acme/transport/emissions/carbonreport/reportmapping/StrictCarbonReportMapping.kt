package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict carbon report mapping records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a strict carbon report mapping is in its life cycle. */
enum class StrictCarbonReportMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One strict carbon report mapping, with what the planners need to act on it. */
data class StrictCarbonReportMapping(
    var code: String,
    var label: String,
    var checksum: String,
    var notes: String,
    var tags: MutableList<String>,
    var endsAt: Int,
    var driverId: String,
    var ceiling: Double,
    var attributes: MutableMap<String, String>,
    var contactEmail: String,
    var currency: String,
    var status: StrictCarbonReportMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCarbonReportMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCarbonReportMappingStatus): Boolean {
        when (status) {
            StrictCarbonReportMappingStatus.OPEN -> return next == StrictCarbonReportMappingStatus.RESOLVED || next == StrictCarbonReportMappingStatus.ARCHIVED
            StrictCarbonReportMappingStatus.IN_REVIEW -> return next == StrictCarbonReportMappingStatus.REOPENED || next == StrictCarbonReportMappingStatus.ARCHIVED
            StrictCarbonReportMappingStatus.RESOLVED -> {
                when (next) {
                    StrictCarbonReportMappingStatus.REOPENED, StrictCarbonReportMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictCarbonReportMappingStatus.REOPENED -> return next == StrictCarbonReportMappingStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictCarbonReportMappingStatus) {
        if (!canMoveTo(next)) {
            error("strict carbon report mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
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

    fun netValue(): Double = round((endsAt.toDouble() - ceiling) * 100) / 100

    fun snapshot(): StrictCarbonReportMapping {
        return StrictCarbonReportMapping(
            code = code,
            label = label,
            checksum = checksum,
            notes = notes,
            tags = tags,
            endsAt = endsAt,
            driverId = driverId,
            ceiling = ceiling,
            attributes = attributes,
            contactEmail = contactEmail,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict carbon report mapping in status open. */
fun newStrictCarbonReportMapping(code: String, label: String): StrictCarbonReportMapping {
    return StrictCarbonReportMapping(
        code = code,
        label = label,
        checksum = "",
        notes = "",
        tags = mutableListOf<String>(),
        endsAt = 0,
        driverId = "",
        ceiling = 0.0,
        attributes = mutableMapOf<String, String>(),
        contactEmail = "",
        currency = "",
        status = StrictCarbonReportMappingStatus.OPEN,
    )
}
