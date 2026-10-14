package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient rate card policy records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a lenient rate card policy is in its life cycle. */
enum class LenientRateCardPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lenient rate card policy. Its code is unique within a tenant. */
data class LenientRateCardPolicy(
    var code: String,
    var name: String,
    var channel: String,
    var floorValue: Double,
    var pallets: Int,
    var revision: Int,
    var capacity: Int,
    var segment: String,
    var discount: Double,
    var status: LenientRateCardPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRateCardPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRateCardPolicyStatus): Boolean {
        when (status) {
            LenientRateCardPolicyStatus.OPEN -> return next == LenientRateCardPolicyStatus.ARCHIVED
            LenientRateCardPolicyStatus.IN_REVIEW -> return next == LenientRateCardPolicyStatus.ARCHIVED
            LenientRateCardPolicyStatus.RESOLVED -> {
                when (next) {
                    LenientRateCardPolicyStatus.REOPENED, LenientRateCardPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientRateCardPolicyStatus.REOPENED -> {
                when (next) {
                    LenientRateCardPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientRateCardPolicyStatus) {
        if (!canMoveTo(next)) {
            error("lenient rate card policy $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun netValue(): Double = round(pallets.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): LenientRateCardPolicy {
        return LenientRateCardPolicy(
            code = code,
            name = name,
            channel = channel,
            floorValue = floorValue,
            pallets = pallets,
            revision = revision,
            capacity = capacity,
            segment = segment,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lenient rate card policy in status open. */
fun newLenientRateCardPolicy(code: String, name: String): LenientRateCardPolicy {
    return LenientRateCardPolicy(
        code = code,
        name = name,
        channel = "",
        floorValue = 0.0,
        pallets = 0,
        revision = 0,
        capacity = 0,
        segment = "",
        discount = 0.0,
        status = LenientRateCardPolicyStatus.OPEN,
    )
}
