package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.text.normalize

/** Where a cross dock batch is in its life cycle. */
enum class CrossDockBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a cross dock batch and its current status. */
data class CrossDockBatch(
    var code: String,
    var name: String,
    var active: Boolean,
    var etaMinutes: Int,
    var carrier: String,
    var status: CrossDockBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockBatchStatus): Boolean {
        when (status) {
            CrossDockBatchStatus.OPEN -> return next == CrossDockBatchStatus.REOPENED
            CrossDockBatchStatus.IN_REVIEW -> return next == CrossDockBatchStatus.RESOLVED || next == CrossDockBatchStatus.ARCHIVED
            CrossDockBatchStatus.RESOLVED -> return next == CrossDockBatchStatus.ARCHIVED
            CrossDockBatchStatus.REOPENED -> return next == CrossDockBatchStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CrossDockBatchStatus) {
        if (!canMoveTo(next)) {
            error("cross dock batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): CrossDockBatch {
        return CrossDockBatch(
            code = code,
            name = name,
            active = active,
            etaMinutes = etaMinutes,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a cross dock batch in status open. */
fun newCrossDockBatch(code: String, name: String): CrossDockBatch {
    return CrossDockBatch(
        code = code,
        name = name,
        active = false,
        etaMinutes = 0,
        carrier = "",
        status = CrossDockBatchStatus.OPEN,
    )
}
