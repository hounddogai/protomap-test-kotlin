package com.acme.transport.crewing.swap.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The swap digest records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a swap digest is in its life cycle. */
enum class SwapDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A swap digest as the swap team keeps it. */
data class SwapDigest(
    var code: String,
    var label: String,
    var floorValue: Double,
    var threshold: Double,
    var createdAt: Int,
    var cost: Double,
    var externalRef: String,
    var carrier: String,
    var status: SwapDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapDigestStatus): Boolean {
        when (status) {
            SwapDigestStatus.PLANNED -> return next == SwapDigestStatus.CANCELLED
            SwapDigestStatus.SCHEDULED -> return next == SwapDigestStatus.IN_PROGRESS
            SwapDigestStatus.IN_PROGRESS -> return next == SwapDigestStatus.COMPLETED || next == SwapDigestStatus.CANCELLED
            SwapDigestStatus.COMPLETED -> return next == SwapDigestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SwapDigestStatus) {
        if (!canMoveTo(next)) {
            error("swap digest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round((threshold - cost) * 100) / 100

    fun snapshot(): SwapDigest {
        return SwapDigest(
            code = code,
            label = label,
            floorValue = floorValue,
            threshold = threshold,
            createdAt = createdAt,
            cost = cost,
            externalRef = externalRef,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a swap digest in status planned. */
fun newSwapDigest(code: String, label: String): SwapDigest {
    return SwapDigest(
        code = code,
        label = label,
        floorValue = 0.0,
        threshold = 0.0,
        createdAt = 0,
        cost = 0.0,
        externalRef = "",
        carrier = "",
        status = SwapDigestStatus.PLANNED,
    )
}
