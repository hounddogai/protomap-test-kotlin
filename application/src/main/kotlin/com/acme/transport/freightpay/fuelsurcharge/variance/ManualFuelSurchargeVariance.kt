package com.acme.transport.freightpay.fuelsurcharge.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual fuel surcharge variance records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a manual fuel surcharge variance is in its life cycle. */
enum class ManualFuelSurchargeVarianceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual fuel surcharge variance. Its code is unique within a tenant. */
data class ManualFuelSurchargeVariance(
    var code: String,
    var label: String,
    var maximum: Double,
    var batchSize: Int,
    var amount: Double,
    var etaMinutes: Int,
    var archived: Boolean,
    var lane: String,
    var phone: String,
    var quantity: Int,
    var status: ManualFuelSurchargeVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualFuelSurchargeVarianceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualFuelSurchargeVarianceStatus): Boolean {
        when (status) {
            ManualFuelSurchargeVarianceStatus.REQUESTED -> return next == ManualFuelSurchargeVarianceStatus.GRANTED || next == ManualFuelSurchargeVarianceStatus.EXPIRED
            ManualFuelSurchargeVarianceStatus.GRANTED -> return next == ManualFuelSurchargeVarianceStatus.REVOKED
            ManualFuelSurchargeVarianceStatus.REVOKED -> return next == ManualFuelSurchargeVarianceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ManualFuelSurchargeVarianceStatus) {
        if (!canMoveTo(next)) {
            error("manual fuel surcharge variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedExposure(): Double = round((amount - batchSize.toDouble()) * 100) / 100

    fun snapshot(): ManualFuelSurchargeVariance {
        return ManualFuelSurchargeVariance(
            code = code,
            label = label,
            maximum = maximum,
            batchSize = batchSize,
            amount = amount,
            etaMinutes = etaMinutes,
            archived = archived,
            lane = lane,
            phone = phone,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual fuel surcharge variance in status requested. */
fun newManualFuelSurchargeVariance(code: String, label: String): ManualFuelSurchargeVariance {
    return ManualFuelSurchargeVariance(
        code = code,
        label = label,
        maximum = 0.0,
        batchSize = 0,
        amount = 0.0,
        etaMinutes = 0,
        archived = false,
        lane = "",
        phone = "",
        quantity = 0,
        status = ManualFuelSurchargeVarianceStatus.REQUESTED,
    )
}
