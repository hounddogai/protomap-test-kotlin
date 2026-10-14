package com.acme.transport.freightpay.ratecard.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default rate card policy is in its life cycle. */
enum class DefaultRateCardPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One default rate card policy, with what the planners need to act on it. */
data class DefaultRateCardPolicy(
    var code: String,
    var label: String,
    var discount: Double,
    var notes: String,
    var distanceKm: Double,
    var checksum: String,
    var rate: Double,
    var ratio: Double,
    var sequence: Int,
    var status: DefaultRateCardPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRateCardPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRateCardPolicyStatus): Boolean {
        when (status) {
            DefaultRateCardPolicyStatus.CREATED -> {
                when (next) {
                    DefaultRateCardPolicyStatus.VALIDATED -> return true
                }
                return false
            }
            DefaultRateCardPolicyStatus.VALIDATED -> {
                when (next) {
                    DefaultRateCardPolicyStatus.PUBLISHED, DefaultRateCardPolicyStatus.RETIRED -> return true
                }
                return false
            }
            DefaultRateCardPolicyStatus.PUBLISHED -> return next == DefaultRateCardPolicyStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultRateCardPolicyStatus) {
        if (!canMoveTo(next)) {
            error("default rate card policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedTotal(): Double = round(sequence.toDouble() * ratio * 100) / 100

    fun snapshot(): DefaultRateCardPolicy {
        return DefaultRateCardPolicy(
            code = code,
            label = label,
            discount = discount,
            notes = notes,
            distanceKm = distanceKm,
            checksum = checksum,
            rate = rate,
            ratio = ratio,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default rate card policy in status created. */
fun newDefaultRateCardPolicy(code: String, label: String): DefaultRateCardPolicy {
    return DefaultRateCardPolicy(
        code = code,
        label = label,
        discount = 0.0,
        notes = "",
        distanceKm = 0.0,
        checksum = "",
        rate = 0.0,
        ratio = 0.0,
        sequence = 0,
        status = DefaultRateCardPolicyStatus.CREATED,
    )
}
