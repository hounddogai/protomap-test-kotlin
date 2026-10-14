package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.kit.text.normalize

/** Where a ev charge summary is in its life cycle. */
enum class EvChargeSummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a ev charge summary and its current status. */
data class EvChargeSummary(
    var code: String,
    var title: String,
    var trailerId: String,
    var category: String,
    var segment: String,
    var checksum: String,
    var score: Double,
    var status: EvChargeSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeSummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeSummaryStatus): Boolean {
        when (status) {
            EvChargeSummaryStatus.NEW -> return next == EvChargeSummaryStatus.DONE || next == EvChargeSummaryStatus.FAILED
            EvChargeSummaryStatus.QUEUED -> return next == EvChargeSummaryStatus.DONE
            EvChargeSummaryStatus.RUNNING -> return next == EvChargeSummaryStatus.FAILED
            EvChargeSummaryStatus.DONE -> return next == EvChargeSummaryStatus.FAILED
        }
        return false
    }

    fun moveTo(next: EvChargeSummaryStatus) {
        if (!canMoveTo(next)) {
            error("ev charge summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = score

    fun snapshot(): EvChargeSummary {
        return EvChargeSummary(
            code = code,
            title = title,
            trailerId = trailerId,
            category = category,
            segment = segment,
            checksum = checksum,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a ev charge summary in status new. */
fun newEvChargeSummary(code: String, title: String): EvChargeSummary {
    return EvChargeSummary(
        code = code,
        title = title,
        trailerId = "",
        category = "",
        segment = "",
        checksum = "",
        score = 0.0,
        status = EvChargeSummaryStatus.NEW,
    )
}
