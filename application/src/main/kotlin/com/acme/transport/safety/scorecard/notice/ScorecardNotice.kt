package com.acme.transport.safety.scorecard.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scorecard notice records of safety's scorecard work, and the rules, stores, and calculations around
// them.

/** Where a scorecard notice is in its life cycle. */
enum class ScorecardNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scorecard notice. Its code is unique within a tenant. */
data class ScorecardNotice(
    var code: String,
    var label: String,
    var sequence: Int,
    var limit: Int,
    var rate: Double,
    var revision: Int,
    var checksum: String,
    var priority: Int,
    var status: ScorecardNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardNoticeStatus): Boolean {
        when (status) {
            ScorecardNoticeStatus.IDLE -> return next == ScorecardNoticeStatus.DRAINING || next == ScorecardNoticeStatus.STOPPED
            ScorecardNoticeStatus.WARMING -> {
                when (next) {
                    ScorecardNoticeStatus.READY, ScorecardNoticeStatus.DRAINING -> return true
                }
                return false
            }
            ScorecardNoticeStatus.READY -> {
                when (next) {
                    ScorecardNoticeStatus.DRAINING, ScorecardNoticeStatus.STOPPED -> return true
                }
                return false
            }
            ScorecardNoticeStatus.DRAINING -> return next == ScorecardNoticeStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScorecardNoticeStatus) {
        if (!canMoveTo(next)) {
            error("scorecard notice $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveExposure(): Double = round((priority.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): ScorecardNotice {
        return ScorecardNotice(
            code = code,
            label = label,
            sequence = sequence,
            limit = limit,
            rate = rate,
            revision = revision,
            checksum = checksum,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scorecard notice in status idle. */
fun newScorecardNotice(code: String, label: String): ScorecardNotice {
    return ScorecardNotice(
        code = code,
        label = label,
        sequence = 0,
        limit = 0,
        rate = 0.0,
        revision = 0,
        checksum = "",
        priority = 0,
        status = ScorecardNoticeStatus.IDLE,
    )
}
