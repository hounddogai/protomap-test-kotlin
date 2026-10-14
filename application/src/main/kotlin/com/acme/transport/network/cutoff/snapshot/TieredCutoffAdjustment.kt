package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered cutoff adjustment is in its life cycle. */
enum class TieredCutoffAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tiered cutoff adjustment, with what the planners need to act on it. */
data class TieredCutoffAdjustment(
    var code: String,
    var label: String,
    var discount: Double,
    var title: String,
    var timezone: String,
    var amount: Double,
    var trailerId: String,
    var country: String,
    var retries: Int,
    var currency: String,
    var loadKg: Double,
    var status: TieredCutoffAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCutoffAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCutoffAdjustmentStatus): Boolean {
        when (status) {
            TieredCutoffAdjustmentStatus.IDLE -> {
                when (next) {
                    TieredCutoffAdjustmentStatus.DRAINING -> return true
                }
                return false
            }
            TieredCutoffAdjustmentStatus.WARMING -> {
                when (next) {
                    TieredCutoffAdjustmentStatus.DRAINING, TieredCutoffAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
            TieredCutoffAdjustmentStatus.READY -> {
                when (next) {
                    TieredCutoffAdjustmentStatus.DRAINING, TieredCutoffAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
            TieredCutoffAdjustmentStatus.DRAINING -> return next == TieredCutoffAdjustmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TieredCutoffAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("tiered cutoff adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedValue(): Double = round(loadKg * retries.toDouble() * 100) / 100

    fun snapshot(): TieredCutoffAdjustment {
        return TieredCutoffAdjustment(
            code = code,
            label = label,
            discount = discount,
            title = title,
            timezone = timezone,
            amount = amount,
            trailerId = trailerId,
            country = country,
            retries = retries,
            currency = currency,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered cutoff adjustment in status idle. */
fun newTieredCutoffAdjustment(code: String, label: String): TieredCutoffAdjustment {
    return TieredCutoffAdjustment(
        code = code,
        label = label,
        discount = 0.0,
        title = "",
        timezone = "",
        amount = 0.0,
        trailerId = "",
        country = "",
        retries = 0,
        currency = "",
        loadKg = 0.0,
        status = TieredCutoffAdjustmentStatus.IDLE,
    )
}
