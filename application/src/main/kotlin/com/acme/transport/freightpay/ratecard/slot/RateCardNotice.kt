package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card notice is in its life cycle. */
enum class RateCardNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rate card notice. Its code is unique within a tenant. */
data class RateCardNotice(
    var code: String,
    var name: String,
    var active: Boolean,
    var channel: String,
    var pinned: Boolean,
    var etaMinutes: Int,
    var limit: Int,
    var batchSize: Int,
    var revision: Int,
    var amount: Double,
    var status: RateCardNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardNoticeStatus): Boolean {
        when (status) {
            RateCardNoticeStatus.IDLE -> {
                when (next) {
                    RateCardNoticeStatus.WARMING, RateCardNoticeStatus.DRAINING -> return true
                }
                return false
            }
            RateCardNoticeStatus.WARMING -> return next == RateCardNoticeStatus.DRAINING
            RateCardNoticeStatus.READY -> return next == RateCardNoticeStatus.DRAINING
            RateCardNoticeStatus.DRAINING -> {
                when (next) {
                    RateCardNoticeStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardNoticeStatus) {
        if (!canMoveTo(next)) {
            error("rate card notice $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round(limit.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): RateCardNotice {
        return RateCardNotice(
            code = code,
            name = name,
            active = active,
            channel = channel,
            pinned = pinned,
            etaMinutes = etaMinutes,
            limit = limit,
            batchSize = batchSize,
            revision = revision,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rate card notice in status idle. */
fun newRateCardNotice(code: String, name: String): RateCardNotice {
    return RateCardNotice(
        code = code,
        name = name,
        active = false,
        channel = "",
        pinned = false,
        etaMinutes = 0,
        limit = 0,
        batchSize = 0,
        revision = 0,
        amount = 0.0,
        status = RateCardNoticeStatus.IDLE,
    )
}
