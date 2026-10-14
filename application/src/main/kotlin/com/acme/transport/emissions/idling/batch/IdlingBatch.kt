package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The idling batch records of emissions's idling work, and the rules, stores, and calculations around them.

/** Where a idling batch is in its life cycle. */
enum class IdlingBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One idling batch, with what the planners need to act on it. */
data class IdlingBatch(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var odometer: Int,
    var attempts: Int,
    var status: IdlingBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingBatchStatus): Boolean {
        when (status) {
            IdlingBatchStatus.DRAFT -> return next == IdlingBatchStatus.PENDING || next == IdlingBatchStatus.SUSPENDED
            IdlingBatchStatus.PENDING -> {
                when (next) {
                    IdlingBatchStatus.ACTIVE, IdlingBatchStatus.SUSPENDED -> return true
                }
                return false
            }
            IdlingBatchStatus.ACTIVE -> return next == IdlingBatchStatus.SUSPENDED
            IdlingBatchStatus.SUSPENDED -> {
                when (next) {
                    IdlingBatchStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingBatchStatus) {
        if (!canMoveTo(next)) {
            error("idling batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"idling batch"} $code"

    fun weightedValue(): Double = round((odometer.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): IdlingBatch {
        return IdlingBatch(
            code = code,
            title = title,
            pinned = pinned,
            odometer = odometer,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a idling batch in status draft. */
fun newIdlingBatch(code: String, title: String): IdlingBatch {
    return IdlingBatch(
        code = code,
        title = title,
        pinned = false,
        odometer = 0,
        attempts = 0,
        status = IdlingBatchStatus.DRAFT,
    )
}
