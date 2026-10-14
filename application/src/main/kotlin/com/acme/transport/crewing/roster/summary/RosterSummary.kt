package com.acme.transport.crewing.roster.summary

import com.acme.transport.kit.text.normalize

// The roster summary records of crewing's roster work, and the rules, stores, and calculations around them.

/** Where a roster summary is in its life cycle. */
enum class RosterSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a roster summary and its current status. */
data class RosterSummary(
    var code: String,
    var label: String,
    var visible: Boolean,
    var reviewer: String,
    var durationMinutes: Int,
    var status: RosterSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterSummaryStatus): Boolean {
        when (status) {
            RosterSummaryStatus.CREATED -> {
                when (next) {
                    RosterSummaryStatus.VALIDATED -> return true
                }
                return false
            }
            RosterSummaryStatus.VALIDATED -> {
                when (next) {
                    RosterSummaryStatus.PUBLISHED, RosterSummaryStatus.RETIRED -> return true
                }
                return false
            }
            RosterSummaryStatus.PUBLISHED -> return next == RosterSummaryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RosterSummaryStatus) {
        if (!canMoveTo(next)) {
            error("roster summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): RosterSummary {
        return RosterSummary(
            code = code,
            label = label,
            visible = visible,
            reviewer = reviewer,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a roster summary in status created. */
fun newRosterSummary(code: String, label: String): RosterSummary {
    return RosterSummary(
        code = code,
        label = label,
        visible = false,
        reviewer = "",
        durationMinutes = 0,
        status = RosterSummaryStatus.CREATED,
    )
}
