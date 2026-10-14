package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rate card bundle records of freightpay's rate card work, and the rules, stores, and calculations around
// them.

/** Where a rate card bundle is in its life cycle. */
enum class RateCardBundleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rate card bundle as the rate card team keeps it. */
data class RateCardBundle(
    var code: String,
    var name: String,
    var category: String,
    var ownerId: String,
    var sequence: Int,
    var discount: Double,
    var checksum: String,
    var currency: String,
    var capacity: Int,
    var status: RateCardBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardBundleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardBundleStatus): Boolean {
        when (status) {
            RateCardBundleStatus.PROPOSED -> {
                when (next) {
                    RateCardBundleStatus.APPROVED, RateCardBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
            RateCardBundleStatus.APPROVED -> {
                when (next) {
                    RateCardBundleStatus.REJECTED, RateCardBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
            RateCardBundleStatus.REJECTED -> {
                when (next) {
                    RateCardBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardBundleStatus) {
        if (!canMoveTo(next)) {
            error("rate card bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round((sequence.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): RateCardBundle {
        return RateCardBundle(
            code = code,
            name = name,
            category = category,
            ownerId = ownerId,
            sequence = sequence,
            discount = discount,
            checksum = checksum,
            currency = currency,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rate card bundle in status proposed. */
fun newRateCardBundle(code: String, name: String): RateCardBundle {
    return RateCardBundle(
        code = code,
        name = name,
        category = "",
        ownerId = "",
        sequence = 0,
        discount = 0.0,
        checksum = "",
        currency = "",
        capacity = 0,
        status = RateCardBundleStatus.PROPOSED,
    )
}
