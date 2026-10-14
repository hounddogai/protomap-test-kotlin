package com.acme.transport.network.cutoff.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cutoff batch records of network's cutoff work, and the rules, stores, and calculations around them.

/** Where a cutoff batch is in its life cycle. */
enum class CutoffBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a cutoff batch and its current status. */
data class CutoffBatch(
    var code: String,
    var title: String,
    var dock: String,
    var etaMinutes: Int,
    var minimum: Double,
    var status: CutoffBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffBatchStatus): Boolean {
        when (status) {
            CutoffBatchStatus.DRAFT -> {
                when (next) {
                    CutoffBatchStatus.CLOSED -> return true
                }
                return false
            }
            CutoffBatchStatus.PENDING -> {
                when (next) {
                    CutoffBatchStatus.ACTIVE, CutoffBatchStatus.CLOSED -> return true
                }
                return false
            }
            CutoffBatchStatus.ACTIVE -> return next == CutoffBatchStatus.SUSPENDED
            CutoffBatchStatus.SUSPENDED -> return next == CutoffBatchStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CutoffBatchStatus) {
        if (!canMoveTo(next)) {
            error("cutoff batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedVolume(): Double = round((minimum - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): CutoffBatch {
        return CutoffBatch(
            code = code,
            title = title,
            dock = dock,
            etaMinutes = etaMinutes,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cutoff batch in status draft. */
fun newCutoffBatch(code: String, title: String): CutoffBatch {
    return CutoffBatch(
        code = code,
        title = title,
        dock = "",
        etaMinutes = 0,
        minimum = 0.0,
        status = CutoffBatchStatus.DRAFT,
    )
}
