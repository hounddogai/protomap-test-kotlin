package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel surcharge review records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a fuel surcharge review is in its life cycle. */
enum class FuelSurchargeReviewStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a fuel surcharge review and its current status. */
data class FuelSurchargeReview(
    var code: String,
    var label: String,
    var country: String,
    var driverId: String,
    var discount: Double,
    var updatedAt: Int,
    var status: FuelSurchargeReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeReviewStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeReviewStatus): Boolean {
        when (status) {
            FuelSurchargeReviewStatus.DRAFT -> return next == FuelSurchargeReviewStatus.CLOSED
            FuelSurchargeReviewStatus.PENDING -> {
                when (next) {
                    FuelSurchargeReviewStatus.CLOSED -> return true
                }
                return false
            }
            FuelSurchargeReviewStatus.ACTIVE -> {
                when (next) {
                    FuelSurchargeReviewStatus.CLOSED -> return true
                }
                return false
            }
            FuelSurchargeReviewStatus.SUSPENDED -> {
                when (next) {
                    FuelSurchargeReviewStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeReviewStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge review $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveExposure(): Double = round((updatedAt.toDouble() + discount) * 100) / 100

    fun snapshot(): FuelSurchargeReview {
        return FuelSurchargeReview(
            code = code,
            label = label,
            country = country,
            driverId = driverId,
            discount = discount,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a fuel surcharge review in status draft. */
fun newFuelSurchargeReview(code: String, label: String): FuelSurchargeReview {
    return FuelSurchargeReview(
        code = code,
        label = label,
        country = "",
        driverId = "",
        discount = 0.0,
        updatedAt = 0,
        status = FuelSurchargeReviewStatus.DRAFT,
    )
}
