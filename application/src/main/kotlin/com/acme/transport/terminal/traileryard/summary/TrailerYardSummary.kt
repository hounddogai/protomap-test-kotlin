package com.acme.transport.terminal.traileryard.summary

import com.acme.transport.kit.text.normalize

// The trailer yard summary records of terminal's trailer yard work, and the rules, stores, and calculations
// around them.

/** Where a trailer yard summary is in its life cycle. */
enum class TrailerYardSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A trailer yard summary as the trailer yard team keeps it. */
data class TrailerYardSummary(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var ownerId: String,
    var notes: String,
    var status: TrailerYardSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardSummaryStatus): Boolean {
        when (status) {
            TrailerYardSummaryStatus.REQUESTED -> return next == TrailerYardSummaryStatus.REVOKED || next == TrailerYardSummaryStatus.EXPIRED
            TrailerYardSummaryStatus.GRANTED -> return next == TrailerYardSummaryStatus.EXPIRED
            TrailerYardSummaryStatus.REVOKED -> return next == TrailerYardSummaryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TrailerYardSummaryStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): TrailerYardSummary {
        return TrailerYardSummary(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            ownerId = ownerId,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a trailer yard summary in status requested. */
fun newTrailerYardSummary(code: String, name: String): TrailerYardSummary {
    return TrailerYardSummary(
        code = code,
        name = name,
        etaMinutes = 0,
        ownerId = "",
        notes = "",
        status = TrailerYardSummaryStatus.REQUESTED,
    )
}
