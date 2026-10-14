package com.acme.transport.terminal.dwell.dwellgroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched dwell group records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a batched dwell group is in its life cycle. */
enum class BatchedDwellGroupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a batched dwell group and its current status. */
data class BatchedDwellGroup(
    var code: String,
    var name: String,
    var source: String,
    var revision: Int,
    var endsAt: Int,
    var discount: Double,
    var shiftCode: String,
    var status: BatchedDwellGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDwellGroupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDwellGroupStatus): Boolean {
        when (status) {
            BatchedDwellGroupStatus.PROPOSED -> return next == BatchedDwellGroupStatus.REJECTED
            BatchedDwellGroupStatus.APPROVED -> return next == BatchedDwellGroupStatus.REJECTED
            BatchedDwellGroupStatus.REJECTED -> return next == BatchedDwellGroupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedDwellGroupStatus) {
        if (!canMoveTo(next)) {
            error("batched dwell group $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun adjustedVolume(): Double = round((discount - revision.toDouble()) * 100) / 100

    fun snapshot(): BatchedDwellGroup {
        return BatchedDwellGroup(
            code = code,
            name = name,
            source = source,
            revision = revision,
            endsAt = endsAt,
            discount = discount,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched dwell group in status proposed. */
fun newBatchedDwellGroup(code: String, name: String): BatchedDwellGroup {
    return BatchedDwellGroup(
        code = code,
        name = name,
        source = "",
        revision = 0,
        endsAt = 0,
        discount = 0.0,
        shiftCode = "",
        status = BatchedDwellGroupStatus.PROPOSED,
    )
}
