package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted scorecard notice is in its life cycle. */
enum class WeightedScorecardNoticeStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a weighted scorecard notice and its current status. */
data class WeightedScorecardNotice(
    var code: String,
    var label: String,
    var vehicleId: String,
    var dock: String,
    var lane: String,
    var threshold: Double,
    var etaMinutes: Int,
    var createdAt: Int,
    var unit: String,
    var status: WeightedScorecardNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedScorecardNoticeStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedScorecardNoticeStatus): Boolean {
        when (status) {
            WeightedScorecardNoticeStatus.REQUESTED -> {
                when (next) {
                    WeightedScorecardNoticeStatus.GRANTED, WeightedScorecardNoticeStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedScorecardNoticeStatus.GRANTED -> return next == WeightedScorecardNoticeStatus.EXPIRED
            WeightedScorecardNoticeStatus.REVOKED -> {
                when (next) {
                    WeightedScorecardNoticeStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedScorecardNoticeStatus) {
        if (!canMoveTo(next)) {
            error("weighted scorecard notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted scorecard notice"} $code"

    fun netLoad(): Double = round((etaMinutes.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedScorecardNotice {
        return WeightedScorecardNotice(
            code = code,
            label = label,
            vehicleId = vehicleId,
            dock = dock,
            lane = lane,
            threshold = threshold,
            etaMinutes = etaMinutes,
            createdAt = createdAt,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted scorecard notice in status requested. */
fun newWeightedScorecardNotice(code: String, label: String): WeightedScorecardNotice {
    return WeightedScorecardNotice(
        code = code,
        label = label,
        vehicleId = "",
        dock = "",
        lane = "",
        threshold = 0.0,
        etaMinutes = 0,
        createdAt = 0,
        unit = "",
        status = WeightedScorecardNoticeStatus.REQUESTED,
    )
}
