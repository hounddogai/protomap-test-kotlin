package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered emission factor batch is in its life cycle. */
enum class TieredEmissionFactorBatchStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a tiered emission factor batch and its current status. */
data class TieredEmissionFactorBatch(
    var code: String,
    var label: String,
    var maximum: Double,
    var depot: String,
    var startsAt: Int,
    var version: Int,
    var timezone: String,
    var odometer: Int,
    var windowDays: Int,
    var status: TieredEmissionFactorBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredEmissionFactorBatchStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredEmissionFactorBatchStatus): Boolean {
        when (status) {
            TieredEmissionFactorBatchStatus.PROPOSED -> return next == TieredEmissionFactorBatchStatus.WITHDRAWN
            TieredEmissionFactorBatchStatus.APPROVED -> return next == TieredEmissionFactorBatchStatus.REJECTED
            TieredEmissionFactorBatchStatus.REJECTED -> return next == TieredEmissionFactorBatchStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredEmissionFactorBatchStatus) {
        if (!canMoveTo(next)) {
            error("tiered emission factor batch $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveValue(): Double = round(maximum * odometer.toDouble() * 100) / 100

    fun snapshot(): TieredEmissionFactorBatch {
        return TieredEmissionFactorBatch(
            code = code,
            label = label,
            maximum = maximum,
            depot = depot,
            startsAt = startsAt,
            version = version,
            timezone = timezone,
            odometer = odometer,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tiered emission factor batch in status proposed. */
fun newTieredEmissionFactorBatch(code: String, label: String): TieredEmissionFactorBatch {
    return TieredEmissionFactorBatch(
        code = code,
        label = label,
        maximum = 0.0,
        depot = "",
        startsAt = 0,
        version = 0,
        timezone = "",
        odometer = 0,
        windowDays = 0,
        status = TieredEmissionFactorBatchStatus.PROPOSED,
    )
}
