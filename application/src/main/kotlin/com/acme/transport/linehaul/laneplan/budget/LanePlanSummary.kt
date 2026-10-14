package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan summary is in its life cycle. */
enum class LanePlanSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a lane plan summary and its current status. */
data class LanePlanSummary(
    var code: String,
    var name: String,
    var contactEmail: String,
    var stopCount: Int,
    var capacity: Int,
    var routeCode: String,
    var windowDays: Int,
    var lane: String,
    var status: LanePlanSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanSummaryStatus): Boolean {
        when (status) {
            LanePlanSummaryStatus.CREATED -> return next == LanePlanSummaryStatus.RETIRED
            LanePlanSummaryStatus.VALIDATED -> {
                when (next) {
                    LanePlanSummaryStatus.PUBLISHED, LanePlanSummaryStatus.RETIRED -> return true
                }
                return false
            }
            LanePlanSummaryStatus.PUBLISHED -> {
                when (next) {
                    LanePlanSummaryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanSummaryStatus) {
        if (!canMoveTo(next)) {
            error("lane plan summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedVolume(): Double = round((windowDays.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): LanePlanSummary {
        return LanePlanSummary(
            code = code,
            name = name,
            contactEmail = contactEmail,
            stopCount = stopCount,
            capacity = capacity,
            routeCode = routeCode,
            windowDays = windowDays,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lane plan summary in status created. */
fun newLanePlanSummary(code: String, name: String): LanePlanSummary {
    return LanePlanSummary(
        code = code,
        name = name,
        contactEmail = "",
        stopCount = 0,
        capacity = 0,
        routeCode = "",
        windowDays = 0,
        lane = "",
        status = LanePlanSummaryStatus.CREATED,
    )
}
