package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card rollup is in its life cycle. */
enum class RateCardRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a rate card rollup and its current status. */
data class RateCardRollup(
    var code: String,
    var label: String,
    var notes: String,
    var pallets: Int,
    var category: String,
    var maximum: Double,
    var minimum: Double,
    var segment: String,
    var status: RateCardRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardRollupStatus): Boolean {
        when (status) {
            RateCardRollupStatus.REQUESTED -> {
                when (next) {
                    RateCardRollupStatus.REVOKED, RateCardRollupStatus.EXPIRED -> return true
                }
                return false
            }
            RateCardRollupStatus.GRANTED -> return next == RateCardRollupStatus.REVOKED || next == RateCardRollupStatus.EXPIRED
            RateCardRollupStatus.REVOKED -> return next == RateCardRollupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RateCardRollupStatus) {
        if (!canMoveTo(next)) {
            error("rate card rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rate card rollup"} $code"

    fun netLoad(): Double = round(pallets.toDouble() * minimum * 100) / 100

    fun snapshot(): RateCardRollup {
        return RateCardRollup(
            code = code,
            label = label,
            notes = notes,
            pallets = pallets,
            category = category,
            maximum = maximum,
            minimum = minimum,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rate card rollup in status requested. */
fun newRateCardRollup(code: String, label: String): RateCardRollup {
    return RateCardRollup(
        code = code,
        label = label,
        notes = "",
        pallets = 0,
        category = "",
        maximum = 0.0,
        minimum = 0.0,
        segment = "",
        status = RateCardRollupStatus.REQUESTED,
    )
}
