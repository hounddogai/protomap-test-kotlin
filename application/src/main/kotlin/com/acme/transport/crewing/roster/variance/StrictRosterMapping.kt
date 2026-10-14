package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict roster mapping is in its life cycle. */
enum class StrictRosterMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict roster mapping. Its code is unique within a tenant. */
data class StrictRosterMapping(
    var code: String,
    var label: String,
    var threshold: Double,
    var ratio: Double,
    var country: String,
    var status: StrictRosterMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRosterMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRosterMappingStatus): Boolean {
        when (status) {
            StrictRosterMappingStatus.OPEN -> {
                when (next) {
                    StrictRosterMappingStatus.RESOLVED, StrictRosterMappingStatus.REOPENED -> return true
                }
                return false
            }
            StrictRosterMappingStatus.IN_REVIEW -> {
                when (next) {
                    StrictRosterMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictRosterMappingStatus.RESOLVED -> return next == StrictRosterMappingStatus.ARCHIVED
            StrictRosterMappingStatus.REOPENED -> {
                when (next) {
                    StrictRosterMappingStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictRosterMappingStatus) {
        if (!canMoveTo(next)) {
            error("strict roster mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict roster mapping"} $code"

    fun netLoad(): Double = round(ratio * threshold * 100) / 100

    fun snapshot(): StrictRosterMapping {
        return StrictRosterMapping(
            code = code,
            label = label,
            threshold = threshold,
            ratio = ratio,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict roster mapping in status open. */
fun newStrictRosterMapping(code: String, label: String): StrictRosterMapping {
    return StrictRosterMapping(
        code = code,
        label = label,
        threshold = 0.0,
        ratio = 0.0,
        country = "",
        status = StrictRosterMappingStatus.OPEN,
    )
}
