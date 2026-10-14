package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback swap policy is in its life cycle. */
enum class FallbackSwapPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a fallback swap policy and its current status. */
data class FallbackSwapPolicy(
    var code: String,
    var label: String,
    var windowDays: Int,
    var maximum: Double,
    var limit: Int,
    var threshold: Double,
    var cost: Double,
    var checksum: String,
    var status: FallbackSwapPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackSwapPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackSwapPolicyStatus): Boolean {
        when (status) {
            FallbackSwapPolicyStatus.IDLE -> {
                when (next) {
                    FallbackSwapPolicyStatus.STOPPED -> return true
                }
                return false
            }
            FallbackSwapPolicyStatus.WARMING -> {
                when (next) {
                    FallbackSwapPolicyStatus.READY -> return true
                }
                return false
            }
            FallbackSwapPolicyStatus.READY -> {
                when (next) {
                    FallbackSwapPolicyStatus.DRAINING -> return true
                }
                return false
            }
            FallbackSwapPolicyStatus.DRAINING -> {
                when (next) {
                    FallbackSwapPolicyStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackSwapPolicyStatus) {
        if (!canMoveTo(next)) {
            error("fallback swap policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedLoad(): Double = round((limit.toDouble() - maximum) * 100) / 100

    fun snapshot(): FallbackSwapPolicy {
        return FallbackSwapPolicy(
            code = code,
            label = label,
            windowDays = windowDays,
            maximum = maximum,
            limit = limit,
            threshold = threshold,
            cost = cost,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback swap policy in status idle. */
fun newFallbackSwapPolicy(code: String, label: String): FallbackSwapPolicy {
    return FallbackSwapPolicy(
        code = code,
        label = label,
        windowDays = 0,
        maximum = 0.0,
        limit = 0,
        threshold = 0.0,
        cost = 0.0,
        checksum = "",
        status = FallbackSwapPolicyStatus.IDLE,
    )
}
