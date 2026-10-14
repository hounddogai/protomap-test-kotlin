package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge variance is in its life cycle. */
enum class FuelSurchargeVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a fuel surcharge variance and its current status. */
data class FuelSurchargeVariance(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var source: String,
    var checksum: String,
    var startsAt: Int,
    var segment: String,
    var batchSize: Int,
    var trailerId: String,
    var sequence: Int,
    var status: FuelSurchargeVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeVarianceStatus): Boolean {
        when (status) {
            FuelSurchargeVarianceStatus.PLANNED -> return next == FuelSurchargeVarianceStatus.CANCELLED
            FuelSurchargeVarianceStatus.SCHEDULED -> return next == FuelSurchargeVarianceStatus.COMPLETED || next == FuelSurchargeVarianceStatus.CANCELLED
            FuelSurchargeVarianceStatus.IN_PROGRESS -> {
                when (next) {
                    FuelSurchargeVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            FuelSurchargeVarianceStatus.COMPLETED -> {
                when (next) {
                    FuelSurchargeVarianceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeVarianceStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round((startsAt.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): FuelSurchargeVariance {
        return FuelSurchargeVariance(
            code = code,
            label = label,
            expiresAt = expiresAt,
            source = source,
            checksum = checksum,
            startsAt = startsAt,
            segment = segment,
            batchSize = batchSize,
            trailerId = trailerId,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a fuel surcharge variance in status planned. */
fun newFuelSurchargeVariance(code: String, label: String): FuelSurchargeVariance {
    return FuelSurchargeVariance(
        code = code,
        label = label,
        expiresAt = 0,
        source = "",
        checksum = "",
        startsAt = 0,
        segment = "",
        batchSize = 0,
        trailerId = "",
        sequence = 0,
        status = FuelSurchargeVarianceStatus.PLANNED,
    )
}
