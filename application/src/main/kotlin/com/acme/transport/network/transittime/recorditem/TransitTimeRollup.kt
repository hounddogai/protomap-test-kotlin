package com.acme.transport.network.transittime.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time rollup is in its life cycle. */
enum class TransitTimeRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a transit time rollup and its current status. */
data class TransitTimeRollup(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var odometer: Int,
    var updatedAt: Int,
    var fuelLiters: Double,
    var status: TransitTimeRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeRollupStatus): Boolean {
        when (status) {
            TransitTimeRollupStatus.CREATED -> return next == TransitTimeRollupStatus.PUBLISHED
            TransitTimeRollupStatus.VALIDATED -> {
                when (next) {
                    TransitTimeRollupStatus.PUBLISHED -> return true
                }
                return false
            }
            TransitTimeRollupStatus.PUBLISHED -> return next == TransitTimeRollupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TransitTimeRollupStatus) {
        if (!canMoveTo(next)) {
            error("transit time rollup $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"transit time rollup"} $code"

    fun weightedVolume(): Double = round((odometer.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): TransitTimeRollup {
        return TransitTimeRollup(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            odometer = odometer,
            updatedAt = updatedAt,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a transit time rollup in status created. */
fun newTransitTimeRollup(code: String, label: String): TransitTimeRollup {
    return TransitTimeRollup(
        code = code,
        label = label,
        etaMinutes = 0,
        odometer = 0,
        updatedAt = 0,
        fuelLiters = 0.0,
        status = TransitTimeRollupStatus.CREATED,
    )
}
