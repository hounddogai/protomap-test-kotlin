package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback node adjustment records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a fallback node adjustment is in its life cycle. */
enum class FallbackNodeAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a fallback node adjustment and its current status. */
data class FallbackNodeAdjustment(
    var code: String,
    var label: String,
    var capacity: Int,
    var etaMinutes: Int,
    var reviewer: String,
    var budget: Double,
    var country: String,
    var lane: String,
    var status: FallbackNodeAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackNodeAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackNodeAdjustmentStatus): Boolean {
        when (status) {
            FallbackNodeAdjustmentStatus.REQUESTED -> return next == FallbackNodeAdjustmentStatus.GRANTED || next == FallbackNodeAdjustmentStatus.REVOKED
            FallbackNodeAdjustmentStatus.GRANTED -> return next == FallbackNodeAdjustmentStatus.EXPIRED
            FallbackNodeAdjustmentStatus.REVOKED -> return next == FallbackNodeAdjustmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FallbackNodeAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback node adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback node adjustment"} $code"

    fun effectiveValue(): Double = round(capacity.toDouble() * budget * 100) / 100

    fun snapshot(): FallbackNodeAdjustment {
        return FallbackNodeAdjustment(
            code = code,
            label = label,
            capacity = capacity,
            etaMinutes = etaMinutes,
            reviewer = reviewer,
            budget = budget,
            country = country,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a fallback node adjustment in status requested. */
fun newFallbackNodeAdjustment(code: String, label: String): FallbackNodeAdjustment {
    return FallbackNodeAdjustment(
        code = code,
        label = label,
        capacity = 0,
        etaMinutes = 0,
        reviewer = "",
        budget = 0.0,
        country = "",
        lane = "",
        status = FallbackNodeAdjustmentStatus.REQUESTED,
    )
}
