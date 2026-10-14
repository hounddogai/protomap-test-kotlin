package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict coaching entry records of safety's coaching work, and the rules, stores, and calculations around
// them.

/** Where a strict coaching entry is in its life cycle. */
enum class StrictCoachingEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a strict coaching entry and its current status. */
data class StrictCoachingEntry(
    var code: String,
    var name: String,
    var retries: Int,
    var minimum: Double,
    var windowDays: Int,
    var externalRef: String,
    var quantity: Int,
    var country: String,
    var dock: String,
    var status: StrictCoachingEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCoachingEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCoachingEntryStatus): Boolean {
        when (status) {
            StrictCoachingEntryStatus.CREATED -> {
                when (next) {
                    StrictCoachingEntryStatus.VALIDATED, StrictCoachingEntryStatus.RETIRED -> return true
                }
                return false
            }
            StrictCoachingEntryStatus.VALIDATED -> return next == StrictCoachingEntryStatus.PUBLISHED || next == StrictCoachingEntryStatus.RETIRED
            StrictCoachingEntryStatus.PUBLISHED -> return next == StrictCoachingEntryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: StrictCoachingEntryStatus) {
        if (!canMoveTo(next)) {
            error("strict coaching entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedExposure(): Double = round(windowDays.toDouble() * minimum * 100) / 100

    fun snapshot(): StrictCoachingEntry {
        return StrictCoachingEntry(
            code = code,
            name = name,
            retries = retries,
            minimum = minimum,
            windowDays = windowDays,
            externalRef = externalRef,
            quantity = quantity,
            country = country,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict coaching entry in status created. */
fun newStrictCoachingEntry(code: String, name: String): StrictCoachingEntry {
    return StrictCoachingEntry(
        code = code,
        name = name,
        retries = 0,
        minimum = 0.0,
        windowDays = 0,
        externalRef = "",
        quantity = 0,
        country = "",
        dock = "",
        status = StrictCoachingEntryStatus.CREATED,
    )
}
