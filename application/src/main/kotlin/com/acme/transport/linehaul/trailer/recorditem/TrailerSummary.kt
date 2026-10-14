package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer summary is in its life cycle. */
enum class TrailerSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One trailer summary, with what the planners need to act on it. */
data class TrailerSummary(
    var code: String,
    var name: String,
    var loadKg: Double,
    var fuelLiters: Double,
    var driverId: String,
    var lane: String,
    var approved: Boolean,
    var score: Double,
    var ceiling: Double,
    var distanceKm: Double,
    var status: TrailerSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerSummaryStatus): Boolean {
        when (status) {
            TrailerSummaryStatus.PLANNED -> return next == TrailerSummaryStatus.IN_PROGRESS || next == TrailerSummaryStatus.CANCELLED
            TrailerSummaryStatus.SCHEDULED -> return next == TrailerSummaryStatus.IN_PROGRESS || next == TrailerSummaryStatus.CANCELLED
            TrailerSummaryStatus.IN_PROGRESS -> {
                when (next) {
                    TrailerSummaryStatus.COMPLETED, TrailerSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            TrailerSummaryStatus.COMPLETED -> return next == TrailerSummaryStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TrailerSummaryStatus) {
        if (!canMoveTo(next)) {
            error("trailer summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netTotal(): Double = round((ceiling + loadKg) * 100) / 100

    fun snapshot(): TrailerSummary {
        return TrailerSummary(
            code = code,
            name = name,
            loadKg = loadKg,
            fuelLiters = fuelLiters,
            driverId = driverId,
            lane = lane,
            approved = approved,
            score = score,
            ceiling = ceiling,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a trailer summary in status planned. */
fun newTrailerSummary(code: String, name: String): TrailerSummary {
    return TrailerSummary(
        code = code,
        name = name,
        loadKg = 0.0,
        fuelLiters = 0.0,
        driverId = "",
        lane = "",
        approved = false,
        score = 0.0,
        ceiling = 0.0,
        distanceKm = 0.0,
        status = TrailerSummaryStatus.PLANNED,
    )
}
