package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard exception is in its life cycle. */
enum class TrailerYardExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a trailer yard exception and its current status. */
data class TrailerYardException(
    var code: String,
    var label: String,
    var title: String,
    var description: String,
    var rate: Double,
    var ownerId: String,
    var loadKg: Double,
    var carrier: String,
    var status: TrailerYardExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardExceptionStatus): Boolean {
        when (status) {
            TrailerYardExceptionStatus.PLANNED -> {
                when (next) {
                    TrailerYardExceptionStatus.IN_PROGRESS -> return true
                }
                return false
            }
            TrailerYardExceptionStatus.SCHEDULED -> return next == TrailerYardExceptionStatus.IN_PROGRESS || next == TrailerYardExceptionStatus.COMPLETED
            TrailerYardExceptionStatus.IN_PROGRESS -> return next == TrailerYardExceptionStatus.COMPLETED
            TrailerYardExceptionStatus.COMPLETED -> return next == TrailerYardExceptionStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TrailerYardExceptionStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedValue(): Double = round((loadKg - rate) * 100) / 100

    fun snapshot(): TrailerYardException {
        return TrailerYardException(
            code = code,
            label = label,
            title = title,
            description = description,
            rate = rate,
            ownerId = ownerId,
            loadKg = loadKg,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer yard exception in status planned. */
fun newTrailerYardException(code: String, label: String): TrailerYardException {
    return TrailerYardException(
        code = code,
        label = label,
        title = "",
        description = "",
        rate = 0.0,
        ownerId = "",
        loadKg = 0.0,
        carrier = "",
        status = TrailerYardExceptionStatus.PLANNED,
    )
}
