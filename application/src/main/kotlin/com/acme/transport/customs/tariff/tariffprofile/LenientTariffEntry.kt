package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient tariff entry is in its life cycle. */
enum class LenientTariffEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One lenient tariff entry, with what the planners need to act on it. */
data class LenientTariffEntry(
    var code: String,
    var label: String,
    var budget: Double,
    var threshold: Double,
    var ratio: Double,
    var lane: String,
    var summary: String,
    var status: LenientTariffEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientTariffEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientTariffEntryStatus): Boolean {
        when (status) {
            LenientTariffEntryStatus.REQUESTED -> {
                when (next) {
                    LenientTariffEntryStatus.REVOKED, LenientTariffEntryStatus.EXPIRED -> return true
                }
                return false
            }
            LenientTariffEntryStatus.GRANTED -> {
                when (next) {
                    LenientTariffEntryStatus.REVOKED, LenientTariffEntryStatus.EXPIRED -> return true
                }
                return false
            }
            LenientTariffEntryStatus.REVOKED -> return next == LenientTariffEntryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LenientTariffEntryStatus) {
        if (!canMoveTo(next)) {
            error("lenient tariff entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lenient tariff entry"} $code"

    fun weightedValue(): Double = round(threshold * budget * 100) / 100

    fun snapshot(): LenientTariffEntry {
        return LenientTariffEntry(
            code = code,
            label = label,
            budget = budget,
            threshold = threshold,
            ratio = ratio,
            lane = lane,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lenient tariff entry in status requested. */
fun newLenientTariffEntry(code: String, label: String): LenientTariffEntry {
    return LenientTariffEntry(
        code = code,
        label = label,
        budget = 0.0,
        threshold = 0.0,
        ratio = 0.0,
        lane = "",
        summary = "",
        status = LenientTariffEntryStatus.REQUESTED,
    )
}
