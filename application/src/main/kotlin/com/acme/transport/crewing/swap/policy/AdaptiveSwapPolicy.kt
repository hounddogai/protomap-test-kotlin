package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive swap policy is in its life cycle. */
enum class AdaptiveSwapPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive swap policy as the swap team keeps it. */
data class AdaptiveSwapPolicy(
    var code: String,
    var label: String,
    var sequence: Int,
    var tags: MutableList<String>,
    var country: String,
    var etaMinutes: Int,
    var odometer: Int,
    var status: AdaptiveSwapPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveSwapPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveSwapPolicyStatus): Boolean {
        when (status) {
            AdaptiveSwapPolicyStatus.REQUESTED -> return next == AdaptiveSwapPolicyStatus.EXPIRED
            AdaptiveSwapPolicyStatus.GRANTED -> {
                when (next) {
                    AdaptiveSwapPolicyStatus.REVOKED, AdaptiveSwapPolicyStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveSwapPolicyStatus.REVOKED -> return next == AdaptiveSwapPolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveSwapPolicyStatus) {
        if (!canMoveTo(next)) {
            error("adaptive swap policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedVolume(): Double = round((odometer.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveSwapPolicy {
        return AdaptiveSwapPolicy(
            code = code,
            label = label,
            sequence = sequence,
            tags = tags,
            country = country,
            etaMinutes = etaMinutes,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive swap policy in status requested. */
fun newAdaptiveSwapPolicy(code: String, label: String): AdaptiveSwapPolicy {
    return AdaptiveSwapPolicy(
        code = code,
        label = label,
        sequence = 0,
        tags = mutableListOf<String>(),
        country = "",
        etaMinutes = 0,
        odometer = 0,
        status = AdaptiveSwapPolicyStatus.REQUESTED,
    )
}
