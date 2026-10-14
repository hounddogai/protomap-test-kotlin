package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.kit.text.normalize

/** Where a rate card entry is in its life cycle. */
enum class RateCardEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a rate card entry and its current status. */
data class RateCardEntry(
    var code: String,
    var name: String,
    var ratio: Double,
    var source: String,
    var summary: String,
    var status: RateCardEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardEntryStatus): Boolean {
        when (status) {
            RateCardEntryStatus.REQUESTED -> {
                when (next) {
                    RateCardEntryStatus.GRANTED, RateCardEntryStatus.EXPIRED -> return true
                }
                return false
            }
            RateCardEntryStatus.GRANTED -> {
                when (next) {
                    RateCardEntryStatus.EXPIRED -> return true
                }
                return false
            }
            RateCardEntryStatus.REVOKED -> {
                when (next) {
                    RateCardEntryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardEntryStatus) {
        if (!canMoveTo(next)) {
            error("rate card entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rate card entry"} $code"

    fun measuredValue(): Double = ratio

    fun snapshot(): RateCardEntry {
        return RateCardEntry(
            code = code,
            name = name,
            ratio = ratio,
            source = source,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rate card entry in status requested. */
fun newRateCardEntry(code: String, name: String): RateCardEntry {
    return RateCardEntry(
        code = code,
        name = name,
        ratio = 0.0,
        source = "",
        summary = "",
        status = RateCardEntryStatus.REQUESTED,
    )
}
