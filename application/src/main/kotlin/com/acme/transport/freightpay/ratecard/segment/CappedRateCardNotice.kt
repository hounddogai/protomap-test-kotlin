package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.text.normalize

/** Where a capped rate card notice is in its life cycle. */
enum class CappedRateCardNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a capped rate card notice and its current status. */
data class CappedRateCardNotice(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var trailerId: String,
    var label: String,
    var margin: Double,
    var status: CappedRateCardNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRateCardNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRateCardNoticeStatus): Boolean {
        when (status) {
            CappedRateCardNoticeStatus.NEW -> return next == CappedRateCardNoticeStatus.QUEUED || next == CappedRateCardNoticeStatus.DONE
            CappedRateCardNoticeStatus.QUEUED -> return next == CappedRateCardNoticeStatus.RUNNING || next == CappedRateCardNoticeStatus.FAILED
            CappedRateCardNoticeStatus.RUNNING -> return next == CappedRateCardNoticeStatus.DONE || next == CappedRateCardNoticeStatus.FAILED
            CappedRateCardNoticeStatus.DONE -> {
                when (next) {
                    CappedRateCardNoticeStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedRateCardNoticeStatus) {
        if (!canMoveTo(next)) {
            error("capped rate card notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($title)"

    fun measuredValue(): Double = margin

    fun snapshot(): CappedRateCardNotice {
        return CappedRateCardNotice(
            code = code,
            title = title,
            pinned = pinned,
            trailerId = trailerId,
            label = label,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped rate card notice in status new. */
fun newCappedRateCardNotice(code: String, title: String): CappedRateCardNotice {
    return CappedRateCardNotice(
        code = code,
        title = title,
        pinned = false,
        trailerId = "",
        label = "",
        margin = 0.0,
        status = CappedRateCardNoticeStatus.NEW,
    )
}
