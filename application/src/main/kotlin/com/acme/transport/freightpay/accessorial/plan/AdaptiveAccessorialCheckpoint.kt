package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive accessorial checkpoint is in its life cycle. */
enum class AdaptiveAccessorialCheckpointStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive accessorial checkpoint as the accessorial team keeps it. */
data class AdaptiveAccessorialCheckpoint(
    var code: String,
    var title: String,
    var contactEmail: String,
    var capacity: Int,
    var quantity: Int,
    var sequence: Int,
    var startsAt: Int,
    var driverId: String,
    var budget: Double,
    var currency: String,
    var status: AdaptiveAccessorialCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAccessorialCheckpointStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAccessorialCheckpointStatus): Boolean {
        when (status) {
            AdaptiveAccessorialCheckpointStatus.NEW -> return next == AdaptiveAccessorialCheckpointStatus.FAILED
            AdaptiveAccessorialCheckpointStatus.QUEUED -> return next == AdaptiveAccessorialCheckpointStatus.DONE || next == AdaptiveAccessorialCheckpointStatus.FAILED
            AdaptiveAccessorialCheckpointStatus.RUNNING -> return next == AdaptiveAccessorialCheckpointStatus.DONE || next == AdaptiveAccessorialCheckpointStatus.FAILED
            AdaptiveAccessorialCheckpointStatus.DONE -> return next == AdaptiveAccessorialCheckpointStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveAccessorialCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("adaptive accessorial checkpoint $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((startsAt.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveAccessorialCheckpoint {
        return AdaptiveAccessorialCheckpoint(
            code = code,
            title = title,
            contactEmail = contactEmail,
            capacity = capacity,
            quantity = quantity,
            sequence = sequence,
            startsAt = startsAt,
            driverId = driverId,
            budget = budget,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive accessorial checkpoint in status new. */
fun newAdaptiveAccessorialCheckpoint(code: String, title: String): AdaptiveAccessorialCheckpoint {
    return AdaptiveAccessorialCheckpoint(
        code = code,
        title = title,
        contactEmail = "",
        capacity = 0,
        quantity = 0,
        sequence = 0,
        startsAt = 0,
        driverId = "",
        budget = 0.0,
        currency = "",
        status = AdaptiveAccessorialCheckpointStatus.NEW,
    )
}
