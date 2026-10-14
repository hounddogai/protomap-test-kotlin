package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary cutoff batch is in its life cycle. */
enum class PrimaryCutoffBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a primary cutoff batch and its current status. */
data class PrimaryCutoffBatch(
    var code: String,
    var name: String,
    var margin: Double,
    var etaMinutes: Int,
    var visible: Boolean,
    var status: PrimaryCutoffBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCutoffBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCutoffBatchStatus): Boolean {
        when (status) {
            PrimaryCutoffBatchStatus.DRAFT -> return next == PrimaryCutoffBatchStatus.CLOSED
            PrimaryCutoffBatchStatus.PENDING -> {
                when (next) {
                    PrimaryCutoffBatchStatus.SUSPENDED -> return true
                }
                return false
            }
            PrimaryCutoffBatchStatus.ACTIVE -> return next == PrimaryCutoffBatchStatus.CLOSED
            PrimaryCutoffBatchStatus.SUSPENDED -> return next == PrimaryCutoffBatchStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PrimaryCutoffBatchStatus) {
        if (!canMoveTo(next)) {
            error("primary cutoff batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round(etaMinutes.toDouble() * margin * 100) / 100

    fun snapshot(): PrimaryCutoffBatch {
        return PrimaryCutoffBatch(
            code = code,
            name = name,
            margin = margin,
            etaMinutes = etaMinutes,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary cutoff batch in status draft. */
fun newPrimaryCutoffBatch(code: String, name: String): PrimaryCutoffBatch {
    return PrimaryCutoffBatch(
        code = code,
        name = name,
        margin = 0.0,
        etaMinutes = 0,
        visible = false,
        status = PrimaryCutoffBatchStatus.DRAFT,
    )
}
