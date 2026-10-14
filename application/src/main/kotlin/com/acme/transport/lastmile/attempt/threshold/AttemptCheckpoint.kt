package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt checkpoint is in its life cycle. */
enum class AttemptCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a attempt checkpoint and its current status. */
data class AttemptCheckpoint(
    var code: String,
    var title: String,
    var ratio: Double,
    var discount: Double,
    var channel: String,
    var status: AttemptCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptCheckpointStatus): Boolean {
        when (status) {
            AttemptCheckpointStatus.REQUESTED -> return next == AttemptCheckpointStatus.EXPIRED
            AttemptCheckpointStatus.GRANTED -> return next == AttemptCheckpointStatus.REVOKED
            AttemptCheckpointStatus.REVOKED -> {
                when (next) {
                    AttemptCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("attempt checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveValue(): Double = round((discount - ratio) * 100) / 100

    fun snapshot(): AttemptCheckpoint {
        return AttemptCheckpoint(
            code = code,
            title = title,
            ratio = ratio,
            discount = discount,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a attempt checkpoint in status requested. */
fun newAttemptCheckpoint(code: String, title: String): AttemptCheckpoint {
    return AttemptCheckpoint(
        code = code,
        title = title,
        ratio = 0.0,
        discount = 0.0,
        channel = "",
        status = AttemptCheckpointStatus.REQUESTED,
    )
}
