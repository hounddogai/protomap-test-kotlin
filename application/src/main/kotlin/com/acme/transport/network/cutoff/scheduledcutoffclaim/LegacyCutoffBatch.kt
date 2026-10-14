package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.text.normalize

/** Where a legacy cutoff batch is in its life cycle. */
enum class LegacyCutoffBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One legacy cutoff batch, with what the planners need to act on it. */
data class LegacyCutoffBatch(
    var code: String,
    var title: String,
    var floorValue: Double,
    var checksum: String,
    var currency: String,
    var status: LegacyCutoffBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCutoffBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCutoffBatchStatus): Boolean {
        when (status) {
            LegacyCutoffBatchStatus.NEW -> return next == LegacyCutoffBatchStatus.RUNNING
            LegacyCutoffBatchStatus.QUEUED -> return next == LegacyCutoffBatchStatus.RUNNING || next == LegacyCutoffBatchStatus.FAILED
            LegacyCutoffBatchStatus.RUNNING -> {
                when (next) {
                    LegacyCutoffBatchStatus.FAILED -> return true
                }
                return false
            }
            LegacyCutoffBatchStatus.DONE -> {
                when (next) {
                    LegacyCutoffBatchStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyCutoffBatchStatus) {
        if (!canMoveTo(next)) {
            error("legacy cutoff batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = floorValue

    fun snapshot(): LegacyCutoffBatch {
        return LegacyCutoffBatch(
            code = code,
            title = title,
            floorValue = floorValue,
            checksum = checksum,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy cutoff batch in status new. */
fun newLegacyCutoffBatch(code: String, title: String): LegacyCutoffBatch {
    return LegacyCutoffBatch(
        code = code,
        title = title,
        floorValue = 0.0,
        checksum = "",
        currency = "",
        status = LegacyCutoffBatchStatus.NEW,
    )
}
