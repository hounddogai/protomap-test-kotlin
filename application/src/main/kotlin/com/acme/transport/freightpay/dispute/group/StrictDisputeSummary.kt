package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict dispute summary is in its life cycle. */
enum class StrictDisputeSummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a strict dispute summary and its current status. */
data class StrictDisputeSummary(
    var code: String,
    var name: String,
    var limit: Int,
    var retries: Int,
    var fuelLiters: Double,
    var durationMinutes: Int,
    var pinned: Boolean,
    var lane: String,
    var distanceKm: Double,
    var notes: String,
    var trailerId: String,
    var status: StrictDisputeSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDisputeSummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDisputeSummaryStatus): Boolean {
        when (status) {
            StrictDisputeSummaryStatus.NEW -> return next == StrictDisputeSummaryStatus.QUEUED || next == StrictDisputeSummaryStatus.RUNNING
            StrictDisputeSummaryStatus.QUEUED -> {
                when (next) {
                    StrictDisputeSummaryStatus.RUNNING, StrictDisputeSummaryStatus.DONE -> return true
                }
                return false
            }
            StrictDisputeSummaryStatus.RUNNING -> return next == StrictDisputeSummaryStatus.DONE || next == StrictDisputeSummaryStatus.FAILED
            StrictDisputeSummaryStatus.DONE -> return next == StrictDisputeSummaryStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictDisputeSummaryStatus) {
        if (!canMoveTo(next)) {
            error("strict dispute summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict dispute summary"} $code"

    fun netVolume(): Double = round((durationMinutes.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): StrictDisputeSummary {
        return StrictDisputeSummary(
            code = code,
            name = name,
            limit = limit,
            retries = retries,
            fuelLiters = fuelLiters,
            durationMinutes = durationMinutes,
            pinned = pinned,
            lane = lane,
            distanceKm = distanceKm,
            notes = notes,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict dispute summary in status new. */
fun newStrictDisputeSummary(code: String, name: String): StrictDisputeSummary {
    return StrictDisputeSummary(
        code = code,
        name = name,
        limit = 0,
        retries = 0,
        fuelLiters = 0.0,
        durationMinutes = 0,
        pinned = false,
        lane = "",
        distanceKm = 0.0,
        notes = "",
        trailerId = "",
        status = StrictDisputeSummaryStatus.NEW,
    )
}
