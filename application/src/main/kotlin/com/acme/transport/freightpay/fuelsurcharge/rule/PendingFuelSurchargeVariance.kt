package com.acme.transport.freightpay.fuelsurcharge.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending fuel surcharge variance is in its life cycle. */
enum class PendingFuelSurchargeVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a pending fuel surcharge variance and its current status. */
data class PendingFuelSurchargeVariance(
    var code: String,
    var title: String,
    var phone: String,
    var floorValue: Double,
    var budget: Double,
    var externalRef: String,
    var name: String,
    var status: PendingFuelSurchargeVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingFuelSurchargeVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingFuelSurchargeVarianceStatus): Boolean {
        when (status) {
            PendingFuelSurchargeVarianceStatus.PLANNED -> return next == PendingFuelSurchargeVarianceStatus.SCHEDULED || next == PendingFuelSurchargeVarianceStatus.COMPLETED
            PendingFuelSurchargeVarianceStatus.SCHEDULED -> {
                when (next) {
                    PendingFuelSurchargeVarianceStatus.COMPLETED, PendingFuelSurchargeVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            PendingFuelSurchargeVarianceStatus.IN_PROGRESS -> {
                when (next) {
                    PendingFuelSurchargeVarianceStatus.COMPLETED -> return true
                }
                return false
            }
            PendingFuelSurchargeVarianceStatus.COMPLETED -> {
                when (next) {
                    PendingFuelSurchargeVarianceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingFuelSurchargeVarianceStatus) {
        if (!canMoveTo(next)) {
            error("pending fuel surcharge variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netVolume(): Double = round(floorValue * budget * 100) / 100

    fun snapshot(): PendingFuelSurchargeVariance {
        return PendingFuelSurchargeVariance(
            code = code,
            title = title,
            phone = phone,
            floorValue = floorValue,
            budget = budget,
            externalRef = externalRef,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending fuel surcharge variance in status planned. */
fun newPendingFuelSurchargeVariance(code: String, title: String): PendingFuelSurchargeVariance {
    return PendingFuelSurchargeVariance(
        code = code,
        title = title,
        phone = "",
        floorValue = 0.0,
        budget = 0.0,
        externalRef = "",
        name = "",
        status = PendingFuelSurchargeVarianceStatus.PLANNED,
    )
}
