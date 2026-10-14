package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal transit time entry is in its life cycle. */
enum class SeasonalTransitTimeEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a seasonal transit time entry and its current status. */
data class SeasonalTransitTimeEntry(
    var code: String,
    var label: String,
    var capacity: Int,
    var category: String,
    var phone: String,
    var budget: Double,
    var status: SeasonalTransitTimeEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTransitTimeEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTransitTimeEntryStatus): Boolean {
        when (status) {
            SeasonalTransitTimeEntryStatus.DRAFT -> return next == SeasonalTransitTimeEntryStatus.PENDING
            SeasonalTransitTimeEntryStatus.PENDING -> {
                when (next) {
                    SeasonalTransitTimeEntryStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalTransitTimeEntryStatus.ACTIVE -> {
                when (next) {
                    SeasonalTransitTimeEntryStatus.SUSPENDED, SeasonalTransitTimeEntryStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalTransitTimeEntryStatus.SUSPENDED -> {
                when (next) {
                    SeasonalTransitTimeEntryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTransitTimeEntryStatus) {
        if (!canMoveTo(next)) {
            error("seasonal transit time entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal transit time entry"} $code"

    fun effectiveLoad(): Double = round(capacity.toDouble() * budget * 100) / 100

    fun snapshot(): SeasonalTransitTimeEntry {
        return SeasonalTransitTimeEntry(
            code = code,
            label = label,
            capacity = capacity,
            category = category,
            phone = phone,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal transit time entry in status draft. */
fun newSeasonalTransitTimeEntry(code: String, label: String): SeasonalTransitTimeEntry {
    return SeasonalTransitTimeEntry(
        code = code,
        label = label,
        capacity = 0,
        category = "",
        phone = "",
        budget = 0.0,
        status = SeasonalTransitTimeEntryStatus.DRAFT,
    )
}
