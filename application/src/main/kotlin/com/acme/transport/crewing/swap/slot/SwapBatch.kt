package com.acme.transport.crewing.swap.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap batch is in its life cycle. */
enum class SwapBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A swap batch as the swap team keeps it. */
data class SwapBatch(
    var code: String,
    var label: String,
    var loadKg: Double,
    var locale: String,
    var archived: Boolean,
    var updatedAt: Int,
    var sequence: Int,
    var version: Int,
    var status: SwapBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapBatchStatus): Boolean {
        when (status) {
            SwapBatchStatus.DRAFT -> {
                when (next) {
                    SwapBatchStatus.SUSPENDED, SwapBatchStatus.CLOSED -> return true
                }
                return false
            }
            SwapBatchStatus.PENDING -> {
                when (next) {
                    SwapBatchStatus.ACTIVE, SwapBatchStatus.CLOSED -> return true
                }
                return false
            }
            SwapBatchStatus.ACTIVE -> {
                when (next) {
                    SwapBatchStatus.CLOSED -> return true
                }
                return false
            }
            SwapBatchStatus.SUSPENDED -> {
                when (next) {
                    SwapBatchStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapBatchStatus) {
        if (!canMoveTo(next)) {
            error("swap batch $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"swap batch"} $code"

    fun adjustedExposure(): Double = round(sequence.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): SwapBatch {
        return SwapBatch(
            code = code,
            label = label,
            loadKg = loadKg,
            locale = locale,
            archived = archived,
            updatedAt = updatedAt,
            sequence = sequence,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a swap batch in status draft. */
fun newSwapBatch(code: String, label: String): SwapBatch {
    return SwapBatch(
        code = code,
        label = label,
        loadKg = 0.0,
        locale = "",
        archived = false,
        updatedAt = 0,
        sequence = 0,
        version = 0,
        status = SwapBatchStatus.DRAFT,
    )
}
