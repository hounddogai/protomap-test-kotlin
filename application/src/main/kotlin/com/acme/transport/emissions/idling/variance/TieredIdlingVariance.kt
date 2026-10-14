package com.acme.transport.emissions.idling.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered idling variance records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a tiered idling variance is in its life cycle. */
enum class TieredIdlingVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a tiered idling variance and its current status. */
data class TieredIdlingVariance(
    var code: String,
    var name: String,
    var floorValue: Double,
    var version: Int,
    var currency: String,
    var expiresAt: Int,
    var trailerId: String,
    var amount: Double,
    var active: Boolean,
    var notes: String,
    var status: TieredIdlingVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredIdlingVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredIdlingVarianceStatus): Boolean {
        when (status) {
            TieredIdlingVarianceStatus.IDLE -> return next == TieredIdlingVarianceStatus.STOPPED
            TieredIdlingVarianceStatus.WARMING -> {
                when (next) {
                    TieredIdlingVarianceStatus.READY, TieredIdlingVarianceStatus.DRAINING -> return true
                }
                return false
            }
            TieredIdlingVarianceStatus.READY -> return next == TieredIdlingVarianceStatus.DRAINING || next == TieredIdlingVarianceStatus.STOPPED
            TieredIdlingVarianceStatus.DRAINING -> {
                when (next) {
                    TieredIdlingVarianceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredIdlingVarianceStatus) {
        if (!canMoveTo(next)) {
            error("tiered idling variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun effectiveExposure(): Double = round((amount - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): TieredIdlingVariance {
        return TieredIdlingVariance(
            code = code,
            name = name,
            floorValue = floorValue,
            version = version,
            currency = currency,
            expiresAt = expiresAt,
            trailerId = trailerId,
            amount = amount,
            active = active,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered idling variance in status idle. */
fun newTieredIdlingVariance(code: String, name: String): TieredIdlingVariance {
    return TieredIdlingVariance(
        code = code,
        name = name,
        floorValue = 0.0,
        version = 0,
        currency = "",
        expiresAt = 0,
        trailerId = "",
        amount = 0.0,
        active = false,
        notes = "",
        status = TieredIdlingVarianceStatus.IDLE,
    )
}
