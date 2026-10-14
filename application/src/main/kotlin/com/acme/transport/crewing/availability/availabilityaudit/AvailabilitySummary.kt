package com.acme.transport.crewing.availability.availabilityaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability summary is in its life cycle. */
enum class AvailabilitySummaryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One availability summary, with what the planners need to act on it. */
data class AvailabilitySummary(
    var code: String,
    var name: String,
    var endsAt: Int,
    var shiftCode: String,
    var windowDays: Int,
    var reviewer: String,
    var odometer: Int,
    var quantity: Int,
    var timezone: String,
    var archived: Boolean,
    var status: AvailabilitySummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilitySummaryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilitySummaryStatus): Boolean {
        when (status) {
            AvailabilitySummaryStatus.IDLE -> return next == AvailabilitySummaryStatus.READY || next == AvailabilitySummaryStatus.DRAINING
            AvailabilitySummaryStatus.WARMING -> {
                when (next) {
                    AvailabilitySummaryStatus.DRAINING, AvailabilitySummaryStatus.STOPPED -> return true
                }
                return false
            }
            AvailabilitySummaryStatus.READY -> {
                when (next) {
                    AvailabilitySummaryStatus.STOPPED -> return true
                }
                return false
            }
            AvailabilitySummaryStatus.DRAINING -> {
                when (next) {
                    AvailabilitySummaryStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilitySummaryStatus) {
        if (!canMoveTo(next)) {
            error("availability summary $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedExposure(): Double = round(windowDays.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): AvailabilitySummary {
        return AvailabilitySummary(
            code = code,
            name = name,
            endsAt = endsAt,
            shiftCode = shiftCode,
            windowDays = windowDays,
            reviewer = reviewer,
            odometer = odometer,
            quantity = quantity,
            timezone = timezone,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a availability summary in status idle. */
fun newAvailabilitySummary(code: String, name: String): AvailabilitySummary {
    return AvailabilitySummary(
        code = code,
        name = name,
        endsAt = 0,
        shiftCode = "",
        windowDays = 0,
        reviewer = "",
        odometer = 0,
        quantity = 0,
        timezone = "",
        archived = false,
        status = AvailabilitySummaryStatus.IDLE,
    )
}
