package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold notice is in its life cycle. */
enum class HoldNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A hold notice. Its code is unique within a tenant. */
data class HoldNotice(
    var code: String,
    var title: String,
    var capacity: Int,
    var ratio: Double,
    var priority: Int,
    var lane: String,
    var driverId: String,
    var status: HoldNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldNoticeStatus): Boolean {
        when (status) {
            HoldNoticeStatus.NEW -> return next == HoldNoticeStatus.QUEUED || next == HoldNoticeStatus.FAILED
            HoldNoticeStatus.QUEUED -> {
                when (next) {
                    HoldNoticeStatus.DONE -> return true
                }
                return false
            }
            HoldNoticeStatus.RUNNING -> {
                when (next) {
                    HoldNoticeStatus.DONE, HoldNoticeStatus.FAILED -> return true
                }
                return false
            }
            HoldNoticeStatus.DONE -> return next == HoldNoticeStatus.FAILED
        }
        return false
    }

    fun moveTo(next: HoldNoticeStatus) {
        if (!canMoveTo(next)) {
            error("hold notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netValue(): Double = round(ratio * priority.toDouble() * 100) / 100

    fun snapshot(): HoldNotice {
        return HoldNotice(
            code = code,
            title = title,
            capacity = capacity,
            ratio = ratio,
            priority = priority,
            lane = lane,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a hold notice in status new. */
fun newHoldNotice(code: String, title: String): HoldNotice {
    return HoldNotice(
        code = code,
        title = title,
        capacity = 0,
        ratio = 0.0,
        priority = 0,
        lane = "",
        driverId = "",
        status = HoldNoticeStatus.NEW,
    )
}
