package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped scorecard notice records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a capped scorecard notice is in its life cycle. */
enum class CappedScorecardNoticeStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A capped scorecard notice. Its code is unique within a tenant. */
data class CappedScorecardNotice(
    var code: String,
    var label: String,
    var priority: Int,
    var ratio: Double,
    var floorValue: Double,
    var updatedAt: Int,
    var version: Int,
    var status: CappedScorecardNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedScorecardNoticeStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedScorecardNoticeStatus): Boolean {
        when (status) {
            CappedScorecardNoticeStatus.OPEN -> return next == CappedScorecardNoticeStatus.IN_REVIEW || next == CappedScorecardNoticeStatus.REOPENED
            CappedScorecardNoticeStatus.IN_REVIEW -> {
                when (next) {
                    CappedScorecardNoticeStatus.REOPENED -> return true
                }
                return false
            }
            CappedScorecardNoticeStatus.RESOLVED -> return next == CappedScorecardNoticeStatus.REOPENED
            CappedScorecardNoticeStatus.REOPENED -> return next == CappedScorecardNoticeStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CappedScorecardNoticeStatus) {
        if (!canMoveTo(next)) {
            error("capped scorecard notice $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"capped scorecard notice"} $code"

    fun weightedLoad(): Double = round(updatedAt.toDouble() * ratio * 100) / 100

    fun snapshot(): CappedScorecardNotice {
        return CappedScorecardNotice(
            code = code,
            label = label,
            priority = priority,
            ratio = ratio,
            floorValue = floorValue,
            updatedAt = updatedAt,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped scorecard notice in status open. */
fun newCappedScorecardNotice(code: String, label: String): CappedScorecardNotice {
    return CappedScorecardNotice(
        code = code,
        label = label,
        priority = 0,
        ratio = 0.0,
        floorValue = 0.0,
        updatedAt = 0,
        version = 0,
        status = CappedScorecardNoticeStatus.OPEN,
    )
}
