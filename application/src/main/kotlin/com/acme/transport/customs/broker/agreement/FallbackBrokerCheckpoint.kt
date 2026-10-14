package com.acme.transport.customs.broker.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback broker checkpoint is in its life cycle. */
enum class FallbackBrokerCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One fallback broker checkpoint, with what the planners need to act on it. */
data class FallbackBrokerCheckpoint(
    var code: String,
    var label: String,
    var amount: Double,
    var graceDays: Int,
    var ownerId: String,
    var reviewer: String,
    var description: String,
    var channel: String,
    var status: FallbackBrokerCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackBrokerCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackBrokerCheckpointStatus): Boolean {
        when (status) {
            FallbackBrokerCheckpointStatus.PROPOSED -> {
                when (next) {
                    FallbackBrokerCheckpointStatus.APPROVED, FallbackBrokerCheckpointStatus.REJECTED -> return true
                }
                return false
            }
            FallbackBrokerCheckpointStatus.APPROVED -> return next == FallbackBrokerCheckpointStatus.REJECTED || next == FallbackBrokerCheckpointStatus.WITHDRAWN
            FallbackBrokerCheckpointStatus.REJECTED -> {
                when (next) {
                    FallbackBrokerCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackBrokerCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("fallback broker checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback broker checkpoint"} $code"

    fun weightedVolume(): Double = round((graceDays.toDouble() + amount) * 100) / 100

    fun snapshot(): FallbackBrokerCheckpoint {
        return FallbackBrokerCheckpoint(
            code = code,
            label = label,
            amount = amount,
            graceDays = graceDays,
            ownerId = ownerId,
            reviewer = reviewer,
            description = description,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fallback broker checkpoint in status proposed. */
fun newFallbackBrokerCheckpoint(code: String, label: String): FallbackBrokerCheckpoint {
    return FallbackBrokerCheckpoint(
        code = code,
        label = label,
        amount = 0.0,
        graceDays = 0,
        ownerId = "",
        reviewer = "",
        description = "",
        channel = "",
        status = FallbackBrokerCheckpointStatus.PROPOSED,
    )
}
