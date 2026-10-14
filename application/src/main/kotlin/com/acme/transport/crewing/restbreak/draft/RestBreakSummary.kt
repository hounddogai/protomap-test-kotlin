package com.acme.transport.crewing.restbreak.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break summary is in its life cycle. */
enum class RestBreakSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a rest break summary and its current status. */
data class RestBreakSummary(
    var code: String,
    var name: String,
    var currency: String,
    var attempts: Int,
    var windowDays: Int,
    var source: String,
    var depot: String,
    var status: RestBreakSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakSummaryStatus): Boolean {
        when (status) {
            RestBreakSummaryStatus.REQUESTED -> return next == RestBreakSummaryStatus.EXPIRED
            RestBreakSummaryStatus.GRANTED -> return next == RestBreakSummaryStatus.REVOKED || next == RestBreakSummaryStatus.EXPIRED
            RestBreakSummaryStatus.REVOKED -> {
                when (next) {
                    RestBreakSummaryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakSummaryStatus) {
        if (!canMoveTo(next)) {
            error("rest break summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedExposure(): Double = round((windowDays.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): RestBreakSummary {
        return RestBreakSummary(
            code = code,
            name = name,
            currency = currency,
            attempts = attempts,
            windowDays = windowDays,
            source = source,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rest break summary in status requested. */
fun newRestBreakSummary(code: String, name: String): RestBreakSummary {
    return RestBreakSummary(
        code = code,
        name = name,
        currency = "",
        attempts = 0,
        windowDays = 0,
        source = "",
        depot = "",
        status = RestBreakSummaryStatus.REQUESTED,
    )
}
