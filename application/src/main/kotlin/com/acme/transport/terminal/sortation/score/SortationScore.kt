package com.acme.transport.terminal.sortation.score

import com.acme.transport.kit.text.normalize

// The sortation score records of terminal's sortation work, and the rules, stores, and calculations around
// them.

/** Where a sortation score is in its life cycle. */
enum class SortationScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a sortation score and its current status. */
data class SortationScore(
    var code: String,
    var label: String,
    var attempts: Int,
    var title: String,
    var timezone: String,
    var country: String,
    var status: SortationScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationScoreStatus): Boolean {
        when (status) {
            SortationScoreStatus.PROPOSED -> return next == SortationScoreStatus.APPROVED || next == SortationScoreStatus.REJECTED
            SortationScoreStatus.APPROVED -> return next == SortationScoreStatus.WITHDRAWN
            SortationScoreStatus.REJECTED -> return next == SortationScoreStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: SortationScoreStatus) {
        if (!canMoveTo(next)) {
            error("sortation score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"sortation score"} $code"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): SortationScore {
        return SortationScore(
            code = code,
            label = label,
            attempts = attempts,
            title = title,
            timezone = timezone,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a sortation score in status proposed. */
fun newSortationScore(code: String, label: String): SortationScore {
    return SortationScore(
        code = code,
        label = label,
        attempts = 0,
        title = "",
        timezone = "",
        country = "",
        status = SortationScoreStatus.PROPOSED,
    )
}
