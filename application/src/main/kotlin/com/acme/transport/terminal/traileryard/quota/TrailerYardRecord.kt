package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.text.normalize

/** Where a trailer yard record is in its life cycle. */
enum class TrailerYardRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a trailer yard record and its current status. */
data class TrailerYardRecord(
    var code: String,
    var name: String,
    var attempts: Int,
    var depot: String,
    var currency: String,
    var status: TrailerYardRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardRecordStatus): Boolean {
        when (status) {
            TrailerYardRecordStatus.IDLE -> {
                when (next) {
                    TrailerYardRecordStatus.READY, TrailerYardRecordStatus.DRAINING -> return true
                }
                return false
            }
            TrailerYardRecordStatus.WARMING -> {
                when (next) {
                    TrailerYardRecordStatus.READY, TrailerYardRecordStatus.STOPPED -> return true
                }
                return false
            }
            TrailerYardRecordStatus.READY -> return next == TrailerYardRecordStatus.DRAINING || next == TrailerYardRecordStatus.STOPPED
            TrailerYardRecordStatus.DRAINING -> {
                when (next) {
                    TrailerYardRecordStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardRecordStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"trailer yard record"} $code"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): TrailerYardRecord {
        return TrailerYardRecord(
            code = code,
            name = name,
            attempts = attempts,
            depot = depot,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a trailer yard record in status idle. */
fun newTrailerYardRecord(code: String, name: String): TrailerYardRecord {
    return TrailerYardRecord(
        code = code,
        name = name,
        attempts = 0,
        depot = "",
        currency = "",
        status = TrailerYardRecordStatus.IDLE,
    )
}
