package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The swap adjustment records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a swap adjustment is in its life cycle. */
enum class SwapAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A swap adjustment. Its code is unique within a tenant. */
data class SwapAdjustment(
    var code: String,
    var label: String,
    var batchSize: Int,
    var durationMinutes: Int,
    var tags: MutableList<String>,
    var status: SwapAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapAdjustmentStatus): Boolean {
        when (status) {
            SwapAdjustmentStatus.NEW -> {
                when (next) {
                    SwapAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            SwapAdjustmentStatus.QUEUED -> {
                when (next) {
                    SwapAdjustmentStatus.RUNNING -> return true
                }
                return false
            }
            SwapAdjustmentStatus.RUNNING -> {
                when (next) {
                    SwapAdjustmentStatus.DONE, SwapAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            SwapAdjustmentStatus.DONE -> {
                when (next) {
                    SwapAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("swap adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"swap adjustment"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round((durationMinutes.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): SwapAdjustment {
        return SwapAdjustment(
            code = code,
            label = label,
            batchSize = batchSize,
            durationMinutes = durationMinutes,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a swap adjustment in status new. */
fun newSwapAdjustment(code: String, label: String): SwapAdjustment {
    return SwapAdjustment(
        code = code,
        label = label,
        batchSize = 0,
        durationMinutes = 0,
        tags = mutableListOf<String>(),
        status = SwapAdjustmentStatus.NEW,
    )
}
