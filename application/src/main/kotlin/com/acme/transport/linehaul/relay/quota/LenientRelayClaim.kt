package com.acme.transport.linehaul.relay.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient relay claim is in its life cycle. */
enum class LenientRelayClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One lenient relay claim, with what the planners need to act on it. */
data class LenientRelayClaim(
    var code: String,
    var label: String,
    var capacity: Int,
    var maximum: Double,
    var source: String,
    var graceDays: Int,
    var stopCount: Int,
    var threshold: Double,
    var region: String,
    var status: LenientRelayClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRelayClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRelayClaimStatus): Boolean {
        when (status) {
            LenientRelayClaimStatus.OPEN -> return next == LenientRelayClaimStatus.IN_REVIEW || next == LenientRelayClaimStatus.RESOLVED
            LenientRelayClaimStatus.IN_REVIEW -> return next == LenientRelayClaimStatus.RESOLVED || next == LenientRelayClaimStatus.REOPENED
            LenientRelayClaimStatus.RESOLVED -> return next == LenientRelayClaimStatus.REOPENED || next == LenientRelayClaimStatus.ARCHIVED
            LenientRelayClaimStatus.REOPENED -> {
                when (next) {
                    LenientRelayClaimStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientRelayClaimStatus) {
        if (!canMoveTo(next)) {
            error("lenient relay claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveTotal(): Double = round((stopCount.toDouble() + maximum) * 100) / 100

    fun snapshot(): LenientRelayClaim {
        return LenientRelayClaim(
            code = code,
            label = label,
            capacity = capacity,
            maximum = maximum,
            source = source,
            graceDays = graceDays,
            stopCount = stopCount,
            threshold = threshold,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lenient relay claim in status open. */
fun newLenientRelayClaim(code: String, label: String): LenientRelayClaim {
    return LenientRelayClaim(
        code = code,
        label = label,
        capacity = 0,
        maximum = 0.0,
        source = "",
        graceDays = 0,
        stopCount = 0,
        threshold = 0.0,
        region = "",
        status = LenientRelayClaimStatus.OPEN,
    )
}
