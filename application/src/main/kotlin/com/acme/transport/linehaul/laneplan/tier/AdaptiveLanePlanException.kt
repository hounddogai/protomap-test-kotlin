package com.acme.transport.linehaul.laneplan.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive lane plan exception is in its life cycle. */
enum class AdaptiveLanePlanExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive lane plan exception as the lane plan team keeps it. */
data class AdaptiveLanePlanException(
    var code: String,
    var title: String,
    var unit: String,
    var currency: String,
    var amount: Double,
    var rate: Double,
    var quantity: Int,
    var limit: Int,
    var depot: String,
    var ceiling: Double,
    var floorValue: Double,
    var status: AdaptiveLanePlanExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLanePlanExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLanePlanExceptionStatus): Boolean {
        when (status) {
            AdaptiveLanePlanExceptionStatus.PLANNED -> return next == AdaptiveLanePlanExceptionStatus.SCHEDULED || next == AdaptiveLanePlanExceptionStatus.IN_PROGRESS
            AdaptiveLanePlanExceptionStatus.SCHEDULED -> return next == AdaptiveLanePlanExceptionStatus.IN_PROGRESS || next == AdaptiveLanePlanExceptionStatus.COMPLETED
            AdaptiveLanePlanExceptionStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveLanePlanExceptionStatus.COMPLETED, AdaptiveLanePlanExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveLanePlanExceptionStatus.COMPLETED -> {
                when (next) {
                    AdaptiveLanePlanExceptionStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveLanePlanExceptionStatus) {
        if (!canMoveTo(next)) {
            error("adaptive lane plan exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netTotal(): Double = round(rate * quantity.toDouble() * 100) / 100

    fun snapshot(): AdaptiveLanePlanException {
        return AdaptiveLanePlanException(
            code = code,
            title = title,
            unit = unit,
            currency = currency,
            amount = amount,
            rate = rate,
            quantity = quantity,
            limit = limit,
            depot = depot,
            ceiling = ceiling,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive lane plan exception in status planned. */
fun newAdaptiveLanePlanException(code: String, title: String): AdaptiveLanePlanException {
    return AdaptiveLanePlanException(
        code = code,
        title = title,
        unit = "",
        currency = "",
        amount = 0.0,
        rate = 0.0,
        quantity = 0,
        limit = 0,
        depot = "",
        ceiling = 0.0,
        floorValue = 0.0,
        status = AdaptiveLanePlanExceptionStatus.PLANNED,
    )
}
