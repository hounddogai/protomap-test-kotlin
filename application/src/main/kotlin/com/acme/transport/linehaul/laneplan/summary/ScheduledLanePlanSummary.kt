package com.acme.transport.linehaul.laneplan.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled lane plan summary records of linehaul's lane plan work, and the rules, stores, and
// calculations around them.

/** Where a scheduled lane plan summary is in its life cycle. */
enum class ScheduledLanePlanSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a scheduled lane plan summary and its current status. */
data class ScheduledLanePlanSummary(
    var code: String,
    var title: String,
    var createdAt: Int,
    var updatedAt: Int,
    var priority: Int,
    var reviewer: String,
    var segment: String,
    var etaMinutes: Int,
    var budget: Double,
    var amount: Double,
    var source: String,
    var status: ScheduledLanePlanSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLanePlanSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLanePlanSummaryStatus): Boolean {
        when (status) {
            ScheduledLanePlanSummaryStatus.REQUESTED -> return next == ScheduledLanePlanSummaryStatus.GRANTED || next == ScheduledLanePlanSummaryStatus.EXPIRED
            ScheduledLanePlanSummaryStatus.GRANTED -> return next == ScheduledLanePlanSummaryStatus.REVOKED || next == ScheduledLanePlanSummaryStatus.EXPIRED
            ScheduledLanePlanSummaryStatus.REVOKED -> {
                when (next) {
                    ScheduledLanePlanSummaryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledLanePlanSummaryStatus) {
        if (!canMoveTo(next)) {
            error("scheduled lane plan summary $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedLoad(): Double = round((updatedAt.toDouble() + budget) * 100) / 100

    fun snapshot(): ScheduledLanePlanSummary {
        return ScheduledLanePlanSummary(
            code = code,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            priority = priority,
            reviewer = reviewer,
            segment = segment,
            etaMinutes = etaMinutes,
            budget = budget,
            amount = amount,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled lane plan summary in status requested. */
fun newScheduledLanePlanSummary(code: String, title: String): ScheduledLanePlanSummary {
    return ScheduledLanePlanSummary(
        code = code,
        title = title,
        createdAt = 0,
        updatedAt = 0,
        priority = 0,
        reviewer = "",
        segment = "",
        etaMinutes = 0,
        budget = 0.0,
        amount = 0.0,
        source = "",
        status = ScheduledLanePlanSummaryStatus.REQUESTED,
    )
}
