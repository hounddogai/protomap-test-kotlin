package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback trailer override is in its life cycle. */
enum class FallbackTrailerOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fallback trailer override, with what the planners need to act on it. */
data class FallbackTrailerOverride(
    var code: String,
    var label: String,
    var pallets: Int,
    var category: String,
    var graceDays: Int,
    var quantity: Int,
    var retries: Int,
    var status: FallbackTrailerOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTrailerOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTrailerOverrideStatus): Boolean {
        when (status) {
            FallbackTrailerOverrideStatus.IDLE -> return next == FallbackTrailerOverrideStatus.STOPPED
            FallbackTrailerOverrideStatus.WARMING -> {
                when (next) {
                    FallbackTrailerOverrideStatus.READY -> return true
                }
                return false
            }
            FallbackTrailerOverrideStatus.READY -> {
                when (next) {
                    FallbackTrailerOverrideStatus.DRAINING, FallbackTrailerOverrideStatus.STOPPED -> return true
                }
                return false
            }
            FallbackTrailerOverrideStatus.DRAINING -> {
                when (next) {
                    FallbackTrailerOverrideStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackTrailerOverrideStatus) {
        if (!canMoveTo(next)) {
            error("fallback trailer override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedLoad(): Double = round((retries.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): FallbackTrailerOverride {
        return FallbackTrailerOverride(
            code = code,
            label = label,
            pallets = pallets,
            category = category,
            graceDays = graceDays,
            quantity = quantity,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fallback trailer override in status idle. */
fun newFallbackTrailerOverride(code: String, label: String): FallbackTrailerOverride {
    return FallbackTrailerOverride(
        code = code,
        label = label,
        pallets = 0,
        category = "",
        graceDays = 0,
        quantity = 0,
        retries = 0,
        status = FallbackTrailerOverrideStatus.IDLE,
    )
}
