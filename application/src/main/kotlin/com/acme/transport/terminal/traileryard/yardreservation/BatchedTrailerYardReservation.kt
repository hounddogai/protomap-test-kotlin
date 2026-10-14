package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched trailer yard reservation records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a batched trailer yard reservation is in its life cycle. */
enum class BatchedTrailerYardReservationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One batched trailer yard reservation, with what the planners need to act on it. */
data class BatchedTrailerYardReservation(
    var code: String,
    var title: String,
    var channel: String,
    var ratio: Double,
    var capacity: Int,
    var label: String,
    var archived: Boolean,
    var loadKg: Double,
    var currency: String,
    var name: String,
    var budget: Double,
    var status: BatchedTrailerYardReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTrailerYardReservationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTrailerYardReservationStatus): Boolean {
        when (status) {
            BatchedTrailerYardReservationStatus.PROPOSED -> return next == BatchedTrailerYardReservationStatus.APPROVED || next == BatchedTrailerYardReservationStatus.REJECTED
            BatchedTrailerYardReservationStatus.APPROVED -> return next == BatchedTrailerYardReservationStatus.REJECTED
            BatchedTrailerYardReservationStatus.REJECTED -> {
                when (next) {
                    BatchedTrailerYardReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTrailerYardReservationStatus) {
        if (!canMoveTo(next)) {
            error("batched trailer yard reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun weightedTotal(): Double = round(loadKg * ratio * 100) / 100

    fun snapshot(): BatchedTrailerYardReservation {
        return BatchedTrailerYardReservation(
            code = code,
            title = title,
            channel = channel,
            ratio = ratio,
            capacity = capacity,
            label = label,
            archived = archived,
            loadKg = loadKg,
            currency = currency,
            name = name,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a batched trailer yard reservation in status proposed. */
fun newBatchedTrailerYardReservation(code: String, title: String): BatchedTrailerYardReservation {
    return BatchedTrailerYardReservation(
        code = code,
        title = title,
        channel = "",
        ratio = 0.0,
        capacity = 0,
        label = "",
        archived = false,
        loadKg = 0.0,
        currency = "",
        name = "",
        budget = 0.0,
        status = BatchedTrailerYardReservationStatus.PROPOSED,
    )
}
