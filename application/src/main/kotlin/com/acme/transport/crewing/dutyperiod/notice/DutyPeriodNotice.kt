package com.acme.transport.crewing.dutyperiod.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The duty period notice records of crewing's duty period work, and the rules, stores, and calculations
// around them.

/** Where a duty period notice is in its life cycle. */
enum class DutyPeriodNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a duty period notice and its current status. */
data class DutyPeriodNotice(
    var code: String,
    var label: String,
    var endsAt: Int,
    var rate: Double,
    var capacity: Int,
    var status: DutyPeriodNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodNoticeStatus): Boolean {
        when (status) {
            DutyPeriodNoticeStatus.PROPOSED -> {
                when (next) {
                    DutyPeriodNoticeStatus.APPROVED, DutyPeriodNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
            DutyPeriodNoticeStatus.APPROVED -> return next == DutyPeriodNoticeStatus.REJECTED || next == DutyPeriodNoticeStatus.WITHDRAWN
            DutyPeriodNoticeStatus.REJECTED -> {
                when (next) {
                    DutyPeriodNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodNoticeStatus) {
        if (!canMoveTo(next)) {
            error("duty period notice $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"duty period notice"} $code"

    fun weightedVolume(): Double = round((capacity.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodNotice {
        return DutyPeriodNotice(
            code = code,
            label = label,
            endsAt = endsAt,
            rate = rate,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a duty period notice in status proposed. */
fun newDutyPeriodNotice(code: String, label: String): DutyPeriodNotice {
    return DutyPeriodNotice(
        code = code,
        label = label,
        endsAt = 0,
        rate = 0.0,
        capacity = 0,
        status = DutyPeriodNoticeStatus.PROPOSED,
    )
}
