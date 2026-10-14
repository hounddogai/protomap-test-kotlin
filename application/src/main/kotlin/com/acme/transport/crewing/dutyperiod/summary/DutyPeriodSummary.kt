package com.acme.transport.crewing.dutyperiod.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The duty period summary records of crewing's duty period work, and the rules, stores, and calculations
// around them.

/** Where a duty period summary is in its life cycle. */
enum class DutyPeriodSummaryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One duty period summary, with what the planners need to act on it. */
data class DutyPeriodSummary(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var ratio: Double,
    var updatedAt: Int,
    var distanceKm: Double,
    var category: String,
    var odometer: Int,
    var windowDays: Int,
    var approved: Boolean,
    var pallets: Int,
    var status: DutyPeriodSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodSummaryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodSummaryStatus): Boolean {
        when (status) {
            DutyPeriodSummaryStatus.IDLE -> return next == DutyPeriodSummaryStatus.READY || next == DutyPeriodSummaryStatus.STOPPED
            DutyPeriodSummaryStatus.WARMING -> return next == DutyPeriodSummaryStatus.READY || next == DutyPeriodSummaryStatus.DRAINING
            DutyPeriodSummaryStatus.READY -> {
                when (next) {
                    DutyPeriodSummaryStatus.DRAINING, DutyPeriodSummaryStatus.STOPPED -> return true
                }
                return false
            }
            DutyPeriodSummaryStatus.DRAINING -> {
                when (next) {
                    DutyPeriodSummaryStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodSummaryStatus) {
        if (!canMoveTo(next)) {
            error("duty period summary $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedVolume(): Double = round(windowDays.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): DutyPeriodSummary {
        return DutyPeriodSummary(
            code = code,
            title = title,
            expiresAt = expiresAt,
            ratio = ratio,
            updatedAt = updatedAt,
            distanceKm = distanceKm,
            category = category,
            odometer = odometer,
            windowDays = windowDays,
            approved = approved,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a duty period summary in status idle. */
fun newDutyPeriodSummary(code: String, title: String): DutyPeriodSummary {
    return DutyPeriodSummary(
        code = code,
        title = title,
        expiresAt = 0,
        ratio = 0.0,
        updatedAt = 0,
        distanceKm = 0.0,
        category = "",
        odometer = 0,
        windowDays = 0,
        approved = false,
        pallets = 0,
        status = DutyPeriodSummaryStatus.IDLE,
    )
}
