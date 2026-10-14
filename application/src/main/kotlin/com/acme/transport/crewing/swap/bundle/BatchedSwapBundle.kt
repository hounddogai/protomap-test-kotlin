package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched swap bundle records of crewing's swap work, and the rules, stores, and calculations around
// them.

/** Where a batched swap bundle is in its life cycle. */
enum class BatchedSwapBundleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched swap bundle. Its code is unique within a tenant. */
data class BatchedSwapBundle(
    var code: String,
    var name: String,
    var margin: Double,
    var trailerId: String,
    var windowDays: Int,
    var ceiling: Double,
    var category: String,
    var endsAt: Int,
    var createdAt: Int,
    var stopCount: Int,
    var minimum: Double,
    var status: BatchedSwapBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedSwapBundleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedSwapBundleStatus): Boolean {
        when (status) {
            BatchedSwapBundleStatus.PROPOSED -> {
                when (next) {
                    BatchedSwapBundleStatus.REJECTED, BatchedSwapBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedSwapBundleStatus.APPROVED -> {
                when (next) {
                    BatchedSwapBundleStatus.REJECTED, BatchedSwapBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedSwapBundleStatus.REJECTED -> return next == BatchedSwapBundleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedSwapBundleStatus) {
        if (!canMoveTo(next)) {
            error("batched swap bundle $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netVolume(): Double = round((stopCount.toDouble() - ceiling) * 100) / 100

    fun snapshot(): BatchedSwapBundle {
        return BatchedSwapBundle(
            code = code,
            name = name,
            margin = margin,
            trailerId = trailerId,
            windowDays = windowDays,
            ceiling = ceiling,
            category = category,
            endsAt = endsAt,
            createdAt = createdAt,
            stopCount = stopCount,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a batched swap bundle in status proposed. */
fun newBatchedSwapBundle(code: String, name: String): BatchedSwapBundle {
    return BatchedSwapBundle(
        code = code,
        name = name,
        margin = 0.0,
        trailerId = "",
        windowDays = 0,
        ceiling = 0.0,
        category = "",
        endsAt = 0,
        createdAt = 0,
        stopCount = 0,
        minimum = 0.0,
        status = BatchedSwapBundleStatus.PROPOSED,
    )
}
