package com.acme.transport.safety.coaching.assignment

import com.acme.transport.kit.text.normalize

/** Where a manual coaching notice is in its life cycle. */
enum class ManualCoachingNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a manual coaching notice and its current status. */
data class ManualCoachingNotice(
    var code: String,
    var title: String,
    var reviewer: String,
    var depot: String,
    var lane: String,
    var visible: Boolean,
    var score: Double,
    var status: ManualCoachingNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCoachingNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCoachingNoticeStatus): Boolean {
        when (status) {
            ManualCoachingNoticeStatus.IDLE -> {
                when (next) {
                    ManualCoachingNoticeStatus.READY, ManualCoachingNoticeStatus.STOPPED -> return true
                }
                return false
            }
            ManualCoachingNoticeStatus.WARMING -> return next == ManualCoachingNoticeStatus.STOPPED
            ManualCoachingNoticeStatus.READY -> return next == ManualCoachingNoticeStatus.DRAINING || next == ManualCoachingNoticeStatus.STOPPED
            ManualCoachingNoticeStatus.DRAINING -> {
                when (next) {
                    ManualCoachingNoticeStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualCoachingNoticeStatus) {
        if (!canMoveTo(next)) {
            error("manual coaching notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual coaching notice"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): ManualCoachingNotice {
        return ManualCoachingNotice(
            code = code,
            title = title,
            reviewer = reviewer,
            depot = depot,
            lane = lane,
            visible = visible,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual coaching notice in status idle. */
fun newManualCoachingNotice(code: String, title: String): ManualCoachingNotice {
    return ManualCoachingNotice(
        code = code,
        title = title,
        reviewer = "",
        depot = "",
        lane = "",
        visible = false,
        score = 0.0,
        status = ManualCoachingNoticeStatus.IDLE,
    )
}
