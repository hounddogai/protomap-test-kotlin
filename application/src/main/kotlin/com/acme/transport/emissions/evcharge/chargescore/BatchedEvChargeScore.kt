package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched ev charge score records of emissions's ev charge work, and the rules, stores, and calculations
// around them.

/** Where a batched ev charge score is in its life cycle. */
enum class BatchedEvChargeScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A batched ev charge score as the ev charge team keeps it. */
data class BatchedEvChargeScore(
    var code: String,
    var name: String,
    var attempts: Int,
    var pinned: Boolean,
    var dock: String,
    var startsAt: Int,
    var amount: Double,
    var odometer: Int,
    var status: BatchedEvChargeScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedEvChargeScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedEvChargeScoreStatus): Boolean {
        when (status) {
            BatchedEvChargeScoreStatus.DRAFT -> return next == BatchedEvChargeScoreStatus.ACTIVE
            BatchedEvChargeScoreStatus.PENDING -> {
                when (next) {
                    BatchedEvChargeScoreStatus.CLOSED -> return true
                }
                return false
            }
            BatchedEvChargeScoreStatus.ACTIVE -> {
                when (next) {
                    BatchedEvChargeScoreStatus.SUSPENDED, BatchedEvChargeScoreStatus.CLOSED -> return true
                }
                return false
            }
            BatchedEvChargeScoreStatus.SUSPENDED -> {
                when (next) {
                    BatchedEvChargeScoreStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedEvChargeScoreStatus) {
        if (!canMoveTo(next)) {
            error("batched ev charge score $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedExposure(): Double = round(odometer.toDouble() * amount * 100) / 100

    fun snapshot(): BatchedEvChargeScore {
        return BatchedEvChargeScore(
            code = code,
            name = name,
            attempts = attempts,
            pinned = pinned,
            dock = dock,
            startsAt = startsAt,
            amount = amount,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched ev charge score in status draft. */
fun newBatchedEvChargeScore(code: String, name: String): BatchedEvChargeScore {
    return BatchedEvChargeScore(
        code = code,
        name = name,
        attempts = 0,
        pinned = false,
        dock = "",
        startsAt = 0,
        amount = 0.0,
        odometer = 0,
        status = BatchedEvChargeScoreStatus.DRAFT,
    )
}
