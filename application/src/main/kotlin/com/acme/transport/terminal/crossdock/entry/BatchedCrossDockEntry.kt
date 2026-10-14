package com.acme.transport.terminal.crossdock.entry

import com.acme.transport.kit.text.normalize

// The batched cross dock entry records of terminal's cross dock work, and the rules, stores, and calculations
// around them.

/** Where a batched cross dock entry is in its life cycle. */
enum class BatchedCrossDockEntryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One batched cross dock entry, with what the planners need to act on it. */
data class BatchedCrossDockEntry(
    var code: String,
    var label: String,
    var approved: Boolean,
    var pinned: Boolean,
    var routeCode: String,
    var name: String,
    var visible: Boolean,
    var phone: String,
    var score: Double,
    var status: BatchedCrossDockEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCrossDockEntryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCrossDockEntryStatus): Boolean {
        when (status) {
            BatchedCrossDockEntryStatus.IDLE -> {
                when (next) {
                    BatchedCrossDockEntryStatus.READY -> return true
                }
                return false
            }
            BatchedCrossDockEntryStatus.WARMING -> return next == BatchedCrossDockEntryStatus.DRAINING
            BatchedCrossDockEntryStatus.READY -> return next == BatchedCrossDockEntryStatus.DRAINING
            BatchedCrossDockEntryStatus.DRAINING -> {
                when (next) {
                    BatchedCrossDockEntryStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedCrossDockEntryStatus) {
        if (!canMoveTo(next)) {
            error("batched cross dock entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = score

    fun snapshot(): BatchedCrossDockEntry {
        return BatchedCrossDockEntry(
            code = code,
            label = label,
            approved = approved,
            pinned = pinned,
            routeCode = routeCode,
            name = name,
            visible = visible,
            phone = phone,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched cross dock entry in status idle. */
fun newBatchedCrossDockEntry(code: String, label: String): BatchedCrossDockEntry {
    return BatchedCrossDockEntry(
        code = code,
        label = label,
        approved = false,
        pinned = false,
        routeCode = "",
        name = "",
        visible = false,
        phone = "",
        score = 0.0,
        status = BatchedCrossDockEntryStatus.IDLE,
    )
}
