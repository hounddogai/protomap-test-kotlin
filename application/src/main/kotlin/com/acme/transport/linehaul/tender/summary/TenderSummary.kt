package com.acme.transport.linehaul.tender.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tender summary records of linehaul's tender work, and the rules, stores, and calculations around them.

/** Where a tender summary is in its life cycle. */
enum class TenderSummaryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a tender summary and its current status. */
data class TenderSummary(
    var code: String,
    var title: String,
    var score: Double,
    var locale: String,
    var budget: Double,
    var depot: String,
    var name: String,
    var unit: String,
    var status: TenderSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderSummaryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderSummaryStatus): Boolean {
        when (status) {
            TenderSummaryStatus.DRAFT -> {
                when (next) {
                    TenderSummaryStatus.ACTIVE -> return true
                }
                return false
            }
            TenderSummaryStatus.PENDING -> return next == TenderSummaryStatus.CLOSED
            TenderSummaryStatus.ACTIVE -> return next == TenderSummaryStatus.CLOSED
            TenderSummaryStatus.SUSPENDED -> {
                when (next) {
                    TenderSummaryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderSummaryStatus) {
        if (!canMoveTo(next)) {
            error("tender summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round(score * budget * 100) / 100

    fun snapshot(): TenderSummary {
        return TenderSummary(
            code = code,
            title = title,
            score = score,
            locale = locale,
            budget = budget,
            depot = depot,
            name = name,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tender summary in status draft. */
fun newTenderSummary(code: String, title: String): TenderSummary {
    return TenderSummary(
        code = code,
        title = title,
        score = 0.0,
        locale = "",
        budget = 0.0,
        depot = "",
        name = "",
        unit = "",
        status = TenderSummaryStatus.DRAFT,
    )
}
