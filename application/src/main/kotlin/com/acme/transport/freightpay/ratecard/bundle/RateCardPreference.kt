package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card preference is in its life cycle. */
enum class RateCardPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rate card preference. Its code is unique within a tenant. */
data class RateCardPreference(
    var code: String,
    var label: String,
    var threshold: Double,
    var lane: String,
    var score: Double,
    var status: RateCardPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardPreferenceStatus): Boolean {
        when (status) {
            RateCardPreferenceStatus.REQUESTED -> return next == RateCardPreferenceStatus.REVOKED
            RateCardPreferenceStatus.GRANTED -> return next == RateCardPreferenceStatus.EXPIRED
            RateCardPreferenceStatus.REVOKED -> {
                when (next) {
                    RateCardPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("rate card preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedLoad(): Double = round((threshold - score) * 100) / 100

    fun snapshot(): RateCardPreference {
        return RateCardPreference(
            code = code,
            label = label,
            threshold = threshold,
            lane = lane,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rate card preference in status requested. */
fun newRateCardPreference(code: String, label: String): RateCardPreference {
    return RateCardPreference(
        code = code,
        label = label,
        threshold = 0.0,
        lane = "",
        score = 0.0,
        status = RateCardPreferenceStatus.REQUESTED,
    )
}
