package com.acme.transport.terminal.dwell.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell batch is in its life cycle. */
enum class DwellBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One dwell batch, with what the planners need to act on it. */
data class DwellBatch(
    var code: String,
    var title: String,
    var threshold: Double,
    var durationMinutes: Int,
    var score: Double,
    var channel: String,
    var currency: String,
    var cost: Double,
    var attempts: Int,
    var ownerId: String,
    var name: String,
    var status: DwellBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellBatchStatus): Boolean {
        when (status) {
            DwellBatchStatus.DRAFT -> return next == DwellBatchStatus.PENDING || next == DwellBatchStatus.SUSPENDED
            DwellBatchStatus.PENDING -> return next == DwellBatchStatus.CLOSED
            DwellBatchStatus.ACTIVE -> {
                when (next) {
                    DwellBatchStatus.CLOSED -> return true
                }
                return false
            }
            DwellBatchStatus.SUSPENDED -> {
                when (next) {
                    DwellBatchStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellBatchStatus) {
        if (!canMoveTo(next)) {
            error("dwell batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netLoad(): Double = round((cost - score) * 100) / 100

    fun snapshot(): DwellBatch {
        return DwellBatch(
            code = code,
            title = title,
            threshold = threshold,
            durationMinutes = durationMinutes,
            score = score,
            channel = channel,
            currency = currency,
            cost = cost,
            attempts = attempts,
            ownerId = ownerId,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a dwell batch in status draft. */
fun newDwellBatch(code: String, title: String): DwellBatch {
    return DwellBatch(
        code = code,
        title = title,
        threshold = 0.0,
        durationMinutes = 0,
        score = 0.0,
        channel = "",
        currency = "",
        cost = 0.0,
        attempts = 0,
        ownerId = "",
        name = "",
        status = DwellBatchStatus.DRAFT,
    )
}
