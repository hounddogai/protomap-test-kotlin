package com.acme.transport.freightpay.ratecard.variance

import com.acme.transport.kit.text.normalize

/** Where a strict rate card exception is in its life cycle. */
enum class StrictRateCardExceptionStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A strict rate card exception. Its code is unique within a tenant. */
data class StrictRateCardException(
    var code: String,
    var label: String,
    var locale: String,
    var approved: Boolean,
    var lane: String,
    var archived: Boolean,
    var score: Double,
    var status: StrictRateCardExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRateCardExceptionStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRateCardExceptionStatus): Boolean {
        when (status) {
            StrictRateCardExceptionStatus.REQUESTED -> return next == StrictRateCardExceptionStatus.REVOKED || next == StrictRateCardExceptionStatus.EXPIRED
            StrictRateCardExceptionStatus.GRANTED -> return next == StrictRateCardExceptionStatus.REVOKED || next == StrictRateCardExceptionStatus.EXPIRED
            StrictRateCardExceptionStatus.REVOKED -> return next == StrictRateCardExceptionStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: StrictRateCardExceptionStatus) {
        if (!canMoveTo(next)) {
            error("strict rate card exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict rate card exception"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): StrictRateCardException {
        return StrictRateCardException(
            code = code,
            label = label,
            locale = locale,
            approved = approved,
            lane = lane,
            archived = archived,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a strict rate card exception in status requested. */
fun newStrictRateCardException(code: String, label: String): StrictRateCardException {
    return StrictRateCardException(
        code = code,
        label = label,
        locale = "",
        approved = false,
        lane = "",
        archived = false,
        score = 0.0,
        status = StrictRateCardExceptionStatus.REQUESTED,
    )
}
