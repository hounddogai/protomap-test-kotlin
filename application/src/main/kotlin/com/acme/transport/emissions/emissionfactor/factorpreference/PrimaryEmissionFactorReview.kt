package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary emission factor review is in its life cycle. */
enum class PrimaryEmissionFactorReviewStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary emission factor review, with what the planners need to act on it. */
data class PrimaryEmissionFactorReview(
    var code: String,
    var label: String,
    var contactEmail: String,
    var country: String,
    var depot: String,
    var weight: Double,
    var currency: String,
    var quantity: Int,
    var endsAt: Int,
    var carrier: String,
    var capacity: Int,
    var status: PrimaryEmissionFactorReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryEmissionFactorReviewStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryEmissionFactorReviewStatus): Boolean {
        when (status) {
            PrimaryEmissionFactorReviewStatus.OPEN -> {
                when (next) {
                    PrimaryEmissionFactorReviewStatus.IN_REVIEW, PrimaryEmissionFactorReviewStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryEmissionFactorReviewStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryEmissionFactorReviewStatus.RESOLVED, PrimaryEmissionFactorReviewStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryEmissionFactorReviewStatus.RESOLVED -> {
                when (next) {
                    PrimaryEmissionFactorReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryEmissionFactorReviewStatus.REOPENED -> {
                when (next) {
                    PrimaryEmissionFactorReviewStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryEmissionFactorReviewStatus) {
        if (!canMoveTo(next)) {
            error("primary emission factor review $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"primary emission factor review"} $code"

    fun weightedValue(): Double = round((quantity.toDouble() - weight) * 100) / 100

    fun snapshot(): PrimaryEmissionFactorReview {
        return PrimaryEmissionFactorReview(
            code = code,
            label = label,
            contactEmail = contactEmail,
            country = country,
            depot = depot,
            weight = weight,
            currency = currency,
            quantity = quantity,
            endsAt = endsAt,
            carrier = carrier,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary emission factor review in status open. */
fun newPrimaryEmissionFactorReview(code: String, label: String): PrimaryEmissionFactorReview {
    return PrimaryEmissionFactorReview(
        code = code,
        label = label,
        contactEmail = "",
        country = "",
        depot = "",
        weight = 0.0,
        currency = "",
        quantity = 0,
        endsAt = 0,
        carrier = "",
        capacity = 0,
        status = PrimaryEmissionFactorReviewStatus.OPEN,
    )
}
