package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict zone summary is in its life cycle. */
enum class StrictZoneSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a strict zone summary and its current status. */
data class StrictZoneSummary(
    var code: String,
    var label: String,
    var approved: Boolean,
    var attempts: Int,
    var lane: String,
    var capacity: Int,
    var vehicleId: String,
    var checksum: String,
    var ratio: Double,
    var cost: Double,
    var status: StrictZoneSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictZoneSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictZoneSummaryStatus): Boolean {
        when (status) {
            StrictZoneSummaryStatus.PROPOSED -> return next == StrictZoneSummaryStatus.APPROVED
            StrictZoneSummaryStatus.APPROVED -> {
                when (next) {
                    StrictZoneSummaryStatus.REJECTED, StrictZoneSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictZoneSummaryStatus.REJECTED -> {
                when (next) {
                    StrictZoneSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictZoneSummaryStatus) {
        if (!canMoveTo(next)) {
            error("strict zone summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedVolume(): Double = round((attempts.toDouble() - ratio) * 100) / 100

    fun snapshot(): StrictZoneSummary {
        return StrictZoneSummary(
            code = code,
            label = label,
            approved = approved,
            attempts = attempts,
            lane = lane,
            capacity = capacity,
            vehicleId = vehicleId,
            checksum = checksum,
            ratio = ratio,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict zone summary in status proposed. */
fun newStrictZoneSummary(code: String, label: String): StrictZoneSummary {
    return StrictZoneSummary(
        code = code,
        label = label,
        approved = false,
        attempts = 0,
        lane = "",
        capacity = 0,
        vehicleId = "",
        checksum = "",
        ratio = 0.0,
        cost = 0.0,
        status = StrictZoneSummaryStatus.PROPOSED,
    )
}
