package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.text.normalize

/** Where a rate card quota is in its life cycle. */
enum class RateCardQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a rate card quota and its current status. */
data class RateCardQuota(
    var code: String,
    var label: String,
    var summary: String,
    var lane: String,
    var approved: Boolean,
    var weight: Double,
    var driverId: String,
    var currency: String,
    var status: RateCardQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardQuotaStatus): Boolean {
        when (status) {
            RateCardQuotaStatus.NEW -> return next == RateCardQuotaStatus.QUEUED || next == RateCardQuotaStatus.RUNNING
            RateCardQuotaStatus.QUEUED -> return next == RateCardQuotaStatus.RUNNING || next == RateCardQuotaStatus.FAILED
            RateCardQuotaStatus.RUNNING -> return next == RateCardQuotaStatus.DONE || next == RateCardQuotaStatus.FAILED
            RateCardQuotaStatus.DONE -> return next == RateCardQuotaStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RateCardQuotaStatus) {
        if (!canMoveTo(next)) {
            error("rate card quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rate card quota"} $code"

    fun measuredValue(): Double = weight

    fun snapshot(): RateCardQuota {
        return RateCardQuota(
            code = code,
            label = label,
            summary = summary,
            lane = lane,
            approved = approved,
            weight = weight,
            driverId = driverId,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rate card quota in status new. */
fun newRateCardQuota(code: String, label: String): RateCardQuota {
    return RateCardQuota(
        code = code,
        label = label,
        summary = "",
        lane = "",
        approved = false,
        weight = 0.0,
        driverId = "",
        currency = "",
        status = RateCardQuotaStatus.NEW,
    )
}
