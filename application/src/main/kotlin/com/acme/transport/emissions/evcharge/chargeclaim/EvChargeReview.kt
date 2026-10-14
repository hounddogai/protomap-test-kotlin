package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge review is in its life cycle. */
enum class EvChargeReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a ev charge review and its current status. */
data class EvChargeReview(
    var code: String,
    var name: String,
    var startsAt: Int,
    var quantity: Int,
    var createdAt: Int,
    var updatedAt: Int,
    var notes: String,
    var status: EvChargeReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeReviewStatus): Boolean {
        when (status) {
            EvChargeReviewStatus.IDLE -> return next == EvChargeReviewStatus.DRAINING
            EvChargeReviewStatus.WARMING -> return next == EvChargeReviewStatus.DRAINING
            EvChargeReviewStatus.READY -> {
                when (next) {
                    EvChargeReviewStatus.DRAINING, EvChargeReviewStatus.STOPPED -> return true
                }
                return false
            }
            EvChargeReviewStatus.DRAINING -> {
                when (next) {
                    EvChargeReviewStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeReviewStatus) {
        if (!canMoveTo(next)) {
            error("ev charge review $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveVolume(): Double = round((startsAt.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): EvChargeReview {
        return EvChargeReview(
            code = code,
            name = name,
            startsAt = startsAt,
            quantity = quantity,
            createdAt = createdAt,
            updatedAt = updatedAt,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a ev charge review in status idle. */
fun newEvChargeReview(code: String, name: String): EvChargeReview {
    return EvChargeReview(
        code = code,
        name = name,
        startsAt = 0,
        quantity = 0,
        createdAt = 0,
        updatedAt = 0,
        notes = "",
        status = EvChargeReviewStatus.IDLE,
    )
}
