package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.text.normalize

// The batched appointment score records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a batched appointment score is in its life cycle. */
enum class BatchedAppointmentScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A batched appointment score as the appointment team keeps it. */
data class BatchedAppointmentScore(
    var code: String,
    var title: String,
    var discount: Double,
    var timezone: String,
    var channel: String,
    var status: BatchedAppointmentScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAppointmentScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAppointmentScoreStatus): Boolean {
        when (status) {
            BatchedAppointmentScoreStatus.DRAFT -> {
                when (next) {
                    BatchedAppointmentScoreStatus.ACTIVE, BatchedAppointmentScoreStatus.SUSPENDED -> return true
                }
                return false
            }
            BatchedAppointmentScoreStatus.PENDING -> {
                when (next) {
                    BatchedAppointmentScoreStatus.CLOSED -> return true
                }
                return false
            }
            BatchedAppointmentScoreStatus.ACTIVE -> return next == BatchedAppointmentScoreStatus.SUSPENDED || next == BatchedAppointmentScoreStatus.CLOSED
            BatchedAppointmentScoreStatus.SUSPENDED -> {
                when (next) {
                    BatchedAppointmentScoreStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedAppointmentScoreStatus) {
        if (!canMoveTo(next)) {
            error("batched appointment score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = discount

    fun snapshot(): BatchedAppointmentScore {
        return BatchedAppointmentScore(
            code = code,
            title = title,
            discount = discount,
            timezone = timezone,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a batched appointment score in status draft. */
fun newBatchedAppointmentScore(code: String, title: String): BatchedAppointmentScore {
    return BatchedAppointmentScore(
        code = code,
        title = title,
        discount = 0.0,
        timezone = "",
        channel = "",
        status = BatchedAppointmentScoreStatus.DRAFT,
    )
}
