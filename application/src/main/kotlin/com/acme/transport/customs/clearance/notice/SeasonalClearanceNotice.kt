package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal clearance notice records of customs's clearance work, and the rules, stores, and calculations
// around them.

/** Where a seasonal clearance notice is in its life cycle. */
enum class SeasonalClearanceNoticeStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a seasonal clearance notice and its current status. */
data class SeasonalClearanceNotice(
    var code: String,
    var title: String,
    var threshold: Double,
    var summary: String,
    var batchSize: Int,
    var vehicleId: String,
    var updatedAt: Int,
    var etaMinutes: Int,
    var segment: String,
    var status: SeasonalClearanceNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalClearanceNoticeStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalClearanceNoticeStatus): Boolean {
        when (status) {
            SeasonalClearanceNoticeStatus.REQUESTED -> return next == SeasonalClearanceNoticeStatus.GRANTED || next == SeasonalClearanceNoticeStatus.EXPIRED
            SeasonalClearanceNoticeStatus.GRANTED -> return next == SeasonalClearanceNoticeStatus.EXPIRED
            SeasonalClearanceNoticeStatus.REVOKED -> {
                when (next) {
                    SeasonalClearanceNoticeStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalClearanceNoticeStatus) {
        if (!canMoveTo(next)) {
            error("seasonal clearance notice $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedTotal(): Double = round((threshold - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): SeasonalClearanceNotice {
        return SeasonalClearanceNotice(
            code = code,
            title = title,
            threshold = threshold,
            summary = summary,
            batchSize = batchSize,
            vehicleId = vehicleId,
            updatedAt = updatedAt,
            etaMinutes = etaMinutes,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a seasonal clearance notice in status requested. */
fun newSeasonalClearanceNotice(code: String, title: String): SeasonalClearanceNotice {
    return SeasonalClearanceNotice(
        code = code,
        title = title,
        threshold = 0.0,
        summary = "",
        batchSize = 0,
        vehicleId = "",
        updatedAt = 0,
        etaMinutes = 0,
        segment = "",
        status = SeasonalClearanceNoticeStatus.REQUESTED,
    )
}
