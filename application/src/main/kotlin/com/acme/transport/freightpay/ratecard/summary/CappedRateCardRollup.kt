package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped rate card rollup is in its life cycle. */
enum class CappedRateCardRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a capped rate card rollup and its current status. */
data class CappedRateCardRollup(
    var code: String,
    var label: String,
    var pallets: Int,
    var sequence: Int,
    var ceiling: Double,
    var unit: String,
    var maximum: Double,
    var windowDays: Int,
    var attempts: Int,
    var revision: Int,
    var capacity: Int,
    var status: CappedRateCardRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRateCardRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRateCardRollupStatus): Boolean {
        when (status) {
            CappedRateCardRollupStatus.REQUESTED -> {
                when (next) {
                    CappedRateCardRollupStatus.GRANTED, CappedRateCardRollupStatus.REVOKED -> return true
                }
                return false
            }
            CappedRateCardRollupStatus.GRANTED -> {
                when (next) {
                    CappedRateCardRollupStatus.REVOKED, CappedRateCardRollupStatus.EXPIRED -> return true
                }
                return false
            }
            CappedRateCardRollupStatus.REVOKED -> return next == CappedRateCardRollupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedRateCardRollupStatus) {
        if (!canMoveTo(next)) {
            error("capped rate card rollup $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round((capacity.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): CappedRateCardRollup {
        return CappedRateCardRollup(
            code = code,
            label = label,
            pallets = pallets,
            sequence = sequence,
            ceiling = ceiling,
            unit = unit,
            maximum = maximum,
            windowDays = windowDays,
            attempts = attempts,
            revision = revision,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a capped rate card rollup in status requested. */
fun newCappedRateCardRollup(code: String, label: String): CappedRateCardRollup {
    return CappedRateCardRollup(
        code = code,
        label = label,
        pallets = 0,
        sequence = 0,
        ceiling = 0.0,
        unit = "",
        maximum = 0.0,
        windowDays = 0,
        attempts = 0,
        revision = 0,
        capacity = 0,
        status = CappedRateCardRollupStatus.REQUESTED,
    )
}
