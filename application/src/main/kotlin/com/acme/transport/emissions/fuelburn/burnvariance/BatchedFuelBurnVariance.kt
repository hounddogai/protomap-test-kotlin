package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched fuel burn variance records of emissions's fuel burn work, and the rules, stores, and
// calculations around them.

/** Where a batched fuel burn variance is in its life cycle. */
enum class BatchedFuelBurnVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched fuel burn variance. Its code is unique within a tenant. */
data class BatchedFuelBurnVariance(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var minimum: Double,
    var startsAt: Int,
    var shiftCode: String,
    var status: BatchedFuelBurnVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedFuelBurnVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedFuelBurnVarianceStatus): Boolean {
        when (status) {
            BatchedFuelBurnVarianceStatus.IDLE -> {
                when (next) {
                    BatchedFuelBurnVarianceStatus.WARMING -> return true
                }
                return false
            }
            BatchedFuelBurnVarianceStatus.WARMING -> {
                when (next) {
                    BatchedFuelBurnVarianceStatus.DRAINING -> return true
                }
                return false
            }
            BatchedFuelBurnVarianceStatus.READY -> {
                when (next) {
                    BatchedFuelBurnVarianceStatus.STOPPED -> return true
                }
                return false
            }
            BatchedFuelBurnVarianceStatus.DRAINING -> {
                when (next) {
                    BatchedFuelBurnVarianceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedFuelBurnVarianceStatus) {
        if (!canMoveTo(next)) {
            error("batched fuel burn variance $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netExposure(): Double = round((distanceKm + minimum) * 100) / 100

    fun snapshot(): BatchedFuelBurnVariance {
        return BatchedFuelBurnVariance(
            code = code,
            label = label,
            distanceKm = distanceKm,
            minimum = minimum,
            startsAt = startsAt,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched fuel burn variance in status idle. */
fun newBatchedFuelBurnVariance(code: String, label: String): BatchedFuelBurnVariance {
    return BatchedFuelBurnVariance(
        code = code,
        label = label,
        distanceKm = 0.0,
        minimum = 0.0,
        startsAt = 0,
        shiftCode = "",
        status = BatchedFuelBurnVarianceStatus.IDLE,
    )
}
