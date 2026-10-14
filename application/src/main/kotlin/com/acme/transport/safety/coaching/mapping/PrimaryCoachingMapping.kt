package com.acme.transport.safety.coaching.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary coaching mapping records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a primary coaching mapping is in its life cycle. */
enum class PrimaryCoachingMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a primary coaching mapping and its current status. */
data class PrimaryCoachingMapping(
    var code: String,
    var title: String,
    var budget: Double,
    var score: Double,
    var tags: MutableList<String>,
    var status: PrimaryCoachingMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCoachingMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCoachingMappingStatus): Boolean {
        when (status) {
            PrimaryCoachingMappingStatus.OPEN -> {
                when (next) {
                    PrimaryCoachingMappingStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryCoachingMappingStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryCoachingMappingStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryCoachingMappingStatus.RESOLVED -> {
                when (next) {
                    PrimaryCoachingMappingStatus.REOPENED, PrimaryCoachingMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryCoachingMappingStatus.REOPENED -> return next == PrimaryCoachingMappingStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryCoachingMappingStatus) {
        if (!canMoveTo(next)) {
            error("primary coaching mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round((score + budget) * 100) / 100

    fun snapshot(): PrimaryCoachingMapping {
        return PrimaryCoachingMapping(
            code = code,
            title = title,
            budget = budget,
            score = score,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary coaching mapping in status open. */
fun newPrimaryCoachingMapping(code: String, title: String): PrimaryCoachingMapping {
    return PrimaryCoachingMapping(
        code = code,
        title = title,
        budget = 0.0,
        score = 0.0,
        tags = mutableListOf<String>(),
        status = PrimaryCoachingMappingStatus.OPEN,
    )
}
