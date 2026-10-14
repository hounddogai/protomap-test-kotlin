package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge draft is in its life cycle. */
enum class FuelSurchargeDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fuel surcharge draft. Its code is unique within a tenant. */
data class FuelSurchargeDraft(
    var code: String,
    var label: String,
    var attempts: Int,
    var driverId: String,
    var source: String,
    var currency: String,
    var createdAt: Int,
    var threshold: Double,
    var status: FuelSurchargeDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeDraftStatus): Boolean {
        when (status) {
            FuelSurchargeDraftStatus.REQUESTED -> return next == FuelSurchargeDraftStatus.GRANTED || next == FuelSurchargeDraftStatus.EXPIRED
            FuelSurchargeDraftStatus.GRANTED -> {
                when (next) {
                    FuelSurchargeDraftStatus.REVOKED, FuelSurchargeDraftStatus.EXPIRED -> return true
                }
                return false
            }
            FuelSurchargeDraftStatus.REVOKED -> return next == FuelSurchargeDraftStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeDraftStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge draft $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round((threshold - createdAt.toDouble()) * 100) / 100

    fun snapshot(): FuelSurchargeDraft {
        return FuelSurchargeDraft(
            code = code,
            label = label,
            attempts = attempts,
            driverId = driverId,
            source = source,
            currency = currency,
            createdAt = createdAt,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a fuel surcharge draft in status requested. */
fun newFuelSurchargeDraft(code: String, label: String): FuelSurchargeDraft {
    return FuelSurchargeDraft(
        code = code,
        label = label,
        attempts = 0,
        driverId = "",
        source = "",
        currency = "",
        createdAt = 0,
        threshold = 0.0,
        status = FuelSurchargeDraftStatus.REQUESTED,
    )
}
