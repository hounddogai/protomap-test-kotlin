package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.kit.text.normalize

/** Where a batched tire threshold is in its life cycle. */
enum class BatchedTireThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a batched tire threshold and its current status. */
data class BatchedTireThreshold(
    var code: String,
    var title: String,
    var country: String,
    var graceDays: Int,
    var dock: String,
    var notes: String,
    var status: BatchedTireThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTireThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTireThresholdStatus): Boolean {
        when (status) {
            BatchedTireThresholdStatus.NEW -> return next == BatchedTireThresholdStatus.RUNNING || next == BatchedTireThresholdStatus.DONE
            BatchedTireThresholdStatus.QUEUED -> {
                when (next) {
                    BatchedTireThresholdStatus.DONE, BatchedTireThresholdStatus.FAILED -> return true
                }
                return false
            }
            BatchedTireThresholdStatus.RUNNING -> return next == BatchedTireThresholdStatus.DONE || next == BatchedTireThresholdStatus.FAILED
            BatchedTireThresholdStatus.DONE -> {
                when (next) {
                    BatchedTireThresholdStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTireThresholdStatus) {
        if (!canMoveTo(next)) {
            error("batched tire threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): BatchedTireThreshold {
        return BatchedTireThreshold(
            code = code,
            title = title,
            country = country,
            graceDays = graceDays,
            dock = dock,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched tire threshold in status new. */
fun newBatchedTireThreshold(code: String, title: String): BatchedTireThreshold {
    return BatchedTireThreshold(
        code = code,
        title = title,
        country = "",
        graceDays = 0,
        dock = "",
        notes = "",
        status = BatchedTireThresholdStatus.NEW,
    )
}
