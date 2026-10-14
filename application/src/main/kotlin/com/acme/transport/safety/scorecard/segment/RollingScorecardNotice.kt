package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling scorecard notice is in its life cycle. */
enum class RollingScorecardNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One rolling scorecard notice, with what the planners need to act on it. */
data class RollingScorecardNotice(
    var code: String,
    var title: String,
    var weight: Double,
    var reviewer: String,
    var updatedAt: Int,
    var amount: Double,
    var pallets: Int,
    var odometer: Int,
    var distanceKm: Double,
    var status: RollingScorecardNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingScorecardNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingScorecardNoticeStatus): Boolean {
        when (status) {
            RollingScorecardNoticeStatus.IDLE -> return next == RollingScorecardNoticeStatus.WARMING || next == RollingScorecardNoticeStatus.DRAINING
            RollingScorecardNoticeStatus.WARMING -> {
                when (next) {
                    RollingScorecardNoticeStatus.READY, RollingScorecardNoticeStatus.DRAINING -> return true
                }
                return false
            }
            RollingScorecardNoticeStatus.READY -> return next == RollingScorecardNoticeStatus.STOPPED
            RollingScorecardNoticeStatus.DRAINING -> return next == RollingScorecardNoticeStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingScorecardNoticeStatus) {
        if (!canMoveTo(next)) {
            error("rolling scorecard notice $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rolling scorecard notice"} $code"

    fun projectedVolume(): Double = round(amount * distanceKm * 100) / 100

    fun snapshot(): RollingScorecardNotice {
        return RollingScorecardNotice(
            code = code,
            title = title,
            weight = weight,
            reviewer = reviewer,
            updatedAt = updatedAt,
            amount = amount,
            pallets = pallets,
            odometer = odometer,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling scorecard notice in status idle. */
fun newRollingScorecardNotice(code: String, title: String): RollingScorecardNotice {
    return RollingScorecardNotice(
        code = code,
        title = title,
        weight = 0.0,
        reviewer = "",
        updatedAt = 0,
        amount = 0.0,
        pallets = 0,
        odometer = 0,
        distanceKm = 0.0,
        status = RollingScorecardNoticeStatus.IDLE,
    )
}
