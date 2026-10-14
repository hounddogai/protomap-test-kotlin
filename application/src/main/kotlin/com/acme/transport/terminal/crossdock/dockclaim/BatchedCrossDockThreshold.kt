package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched cross dock threshold is in its life cycle. */
enum class BatchedCrossDockThresholdStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One batched cross dock threshold, with what the planners need to act on it. */
data class BatchedCrossDockThreshold(
    var code: String,
    var title: String,
    var contactEmail: String,
    var durationMinutes: Int,
    var timezone: String,
    var windowDays: Int,
    var carrier: String,
    var status: BatchedCrossDockThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCrossDockThresholdStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCrossDockThresholdStatus): Boolean {
        when (status) {
            BatchedCrossDockThresholdStatus.DRAFT -> {
                when (next) {
                    BatchedCrossDockThresholdStatus.CLOSED -> return true
                }
                return false
            }
            BatchedCrossDockThresholdStatus.PENDING -> return next == BatchedCrossDockThresholdStatus.CLOSED
            BatchedCrossDockThresholdStatus.ACTIVE -> return next == BatchedCrossDockThresholdStatus.CLOSED
            BatchedCrossDockThresholdStatus.SUSPENDED -> return next == BatchedCrossDockThresholdStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BatchedCrossDockThresholdStatus) {
        if (!canMoveTo(next)) {
            error("batched cross dock threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netLoad(): Double = round((durationMinutes.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): BatchedCrossDockThreshold {
        return BatchedCrossDockThreshold(
            code = code,
            title = title,
            contactEmail = contactEmail,
            durationMinutes = durationMinutes,
            timezone = timezone,
            windowDays = windowDays,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched cross dock threshold in status draft. */
fun newBatchedCrossDockThreshold(code: String, title: String): BatchedCrossDockThreshold {
    return BatchedCrossDockThreshold(
        code = code,
        title = title,
        contactEmail = "",
        durationMinutes = 0,
        timezone = "",
        windowDays = 0,
        carrier = "",
        status = BatchedCrossDockThresholdStatus.DRAFT,
    )
}
