package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive backhaul adjustment records of linehaul's backhaul work, and the rules, stores, and
// calculations around them.

/** Where a adaptive backhaul adjustment is in its life cycle. */
enum class AdaptiveBackhaulAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive backhaul adjustment. Its code is unique within a tenant. */
data class AdaptiveBackhaulAdjustment(
    var code: String,
    var label: String,
    var floorValue: Double,
    var ceiling: Double,
    var createdAt: Int,
    var batchSize: Int,
    var ratio: Double,
    var attempts: Int,
    var checksum: String,
    var approved: Boolean,
    var status: AdaptiveBackhaulAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveBackhaulAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveBackhaulAdjustmentStatus): Boolean {
        when (status) {
            AdaptiveBackhaulAdjustmentStatus.IDLE -> return next == AdaptiveBackhaulAdjustmentStatus.STOPPED
            AdaptiveBackhaulAdjustmentStatus.WARMING -> return next == AdaptiveBackhaulAdjustmentStatus.READY || next == AdaptiveBackhaulAdjustmentStatus.DRAINING
            AdaptiveBackhaulAdjustmentStatus.READY -> return next == AdaptiveBackhaulAdjustmentStatus.DRAINING || next == AdaptiveBackhaulAdjustmentStatus.STOPPED
            AdaptiveBackhaulAdjustmentStatus.DRAINING -> {
                when (next) {
                    AdaptiveBackhaulAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveBackhaulAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("adaptive backhaul adjustment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedTotal(): Double = round((ceiling + createdAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveBackhaulAdjustment {
        return AdaptiveBackhaulAdjustment(
            code = code,
            label = label,
            floorValue = floorValue,
            ceiling = ceiling,
            createdAt = createdAt,
            batchSize = batchSize,
            ratio = ratio,
            attempts = attempts,
            checksum = checksum,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive backhaul adjustment in status idle. */
fun newAdaptiveBackhaulAdjustment(code: String, label: String): AdaptiveBackhaulAdjustment {
    return AdaptiveBackhaulAdjustment(
        code = code,
        label = label,
        floorValue = 0.0,
        ceiling = 0.0,
        createdAt = 0,
        batchSize = 0,
        ratio = 0.0,
        attempts = 0,
        checksum = "",
        approved = false,
        status = AdaptiveBackhaulAdjustmentStatus.IDLE,
    )
}
