package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered service area forecast is in its life cycle. */
enum class TieredServiceAreaForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered service area forecast. Its code is unique within a tenant. */
data class TieredServiceAreaForecast(
    var code: String,
    var label: String,
    var pallets: Int,
    var quantity: Int,
    var attempts: Int,
    var endsAt: Int,
    var status: TieredServiceAreaForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredServiceAreaForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredServiceAreaForecastStatus): Boolean {
        when (status) {
            TieredServiceAreaForecastStatus.PROPOSED -> return next == TieredServiceAreaForecastStatus.APPROVED || next == TieredServiceAreaForecastStatus.REJECTED
            TieredServiceAreaForecastStatus.APPROVED -> return next == TieredServiceAreaForecastStatus.REJECTED
            TieredServiceAreaForecastStatus.REJECTED -> {
                when (next) {
                    TieredServiceAreaForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredServiceAreaForecastStatus) {
        if (!canMoveTo(next)) {
            error("tiered service area forecast $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round((endsAt.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): TieredServiceAreaForecast {
        return TieredServiceAreaForecast(
            code = code,
            label = label,
            pallets = pallets,
            quantity = quantity,
            attempts = attempts,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered service area forecast in status proposed. */
fun newTieredServiceAreaForecast(code: String, label: String): TieredServiceAreaForecast {
    return TieredServiceAreaForecast(
        code = code,
        label = label,
        pallets = 0,
        quantity = 0,
        attempts = 0,
        endsAt = 0,
        status = TieredServiceAreaForecastStatus.PROPOSED,
    )
}
