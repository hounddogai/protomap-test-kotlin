package com.acme.transport.fleetops.defect.defectsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal defect notice is in its life cycle. */
enum class SeasonalDefectNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One seasonal defect notice, with what the planners need to act on it. */
data class SeasonalDefectNotice(
    var code: String,
    var name: String,
    var score: Double,
    var discount: Double,
    var active: Boolean,
    var attempts: Int,
    var status: SeasonalDefectNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDefectNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDefectNoticeStatus): Boolean {
        when (status) {
            SeasonalDefectNoticeStatus.PROPOSED -> return next == SeasonalDefectNoticeStatus.APPROVED || next == SeasonalDefectNoticeStatus.REJECTED
            SeasonalDefectNoticeStatus.APPROVED -> return next == SeasonalDefectNoticeStatus.REJECTED
            SeasonalDefectNoticeStatus.REJECTED -> {
                when (next) {
                    SeasonalDefectNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDefectNoticeStatus) {
        if (!canMoveTo(next)) {
            error("seasonal defect notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal defect notice"} $code"

    fun projectedVolume(): Double = round(score * discount * 100) / 100

    fun snapshot(): SeasonalDefectNotice {
        return SeasonalDefectNotice(
            code = code,
            name = name,
            score = score,
            discount = discount,
            active = active,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal defect notice in status proposed. */
fun newSeasonalDefectNotice(code: String, name: String): SeasonalDefectNotice {
    return SeasonalDefectNotice(
        code = code,
        name = name,
        score = 0.0,
        discount = 0.0,
        active = false,
        attempts = 0,
        status = SeasonalDefectNoticeStatus.PROPOSED,
    )
}
