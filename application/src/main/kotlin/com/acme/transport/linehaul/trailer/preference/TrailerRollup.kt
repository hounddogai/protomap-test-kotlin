package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.text.normalize

/** Where a trailer rollup is in its life cycle. */
enum class TrailerRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a trailer rollup and its current status. */
data class TrailerRollup(
    var code: String,
    var name: String,
    var lane: String,
    var phone: String,
    var carrier: String,
    var currency: String,
    var contactEmail: String,
    var quantity: Int,
    var status: TrailerRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerRollupStatus): Boolean {
        when (status) {
            TrailerRollupStatus.PLANNED -> return next == TrailerRollupStatus.COMPLETED || next == TrailerRollupStatus.CANCELLED
            TrailerRollupStatus.SCHEDULED -> return next == TrailerRollupStatus.CANCELLED
            TrailerRollupStatus.IN_PROGRESS -> {
                when (next) {
                    TrailerRollupStatus.COMPLETED -> return true
                }
                return false
            }
            TrailerRollupStatus.COMPLETED -> {
                when (next) {
                    TrailerRollupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerRollupStatus) {
        if (!canMoveTo(next)) {
            error("trailer rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"trailer rollup"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): TrailerRollup {
        return TrailerRollup(
            code = code,
            name = name,
            lane = lane,
            phone = phone,
            carrier = carrier,
            currency = currency,
            contactEmail = contactEmail,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a trailer rollup in status planned. */
fun newTrailerRollup(code: String, name: String): TrailerRollup {
    return TrailerRollup(
        code = code,
        name = name,
        lane = "",
        phone = "",
        carrier = "",
        currency = "",
        contactEmail = "",
        quantity = 0,
        status = TrailerRollupStatus.PLANNED,
    )
}
