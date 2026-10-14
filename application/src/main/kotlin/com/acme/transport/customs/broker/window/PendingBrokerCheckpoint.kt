package com.acme.transport.customs.broker.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending broker checkpoint is in its life cycle. */
enum class PendingBrokerCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a pending broker checkpoint and its current status. */
data class PendingBrokerCheckpoint(
    var code: String,
    var name: String,
    var ceiling: Double,
    var externalRef: String,
    var summary: String,
    var graceDays: Int,
    var threshold: Double,
    var status: PendingBrokerCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingBrokerCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingBrokerCheckpointStatus): Boolean {
        when (status) {
            PendingBrokerCheckpointStatus.PROPOSED -> return next == PendingBrokerCheckpointStatus.APPROVED
            PendingBrokerCheckpointStatus.APPROVED -> {
                when (next) {
                    PendingBrokerCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingBrokerCheckpointStatus.REJECTED -> return next == PendingBrokerCheckpointStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PendingBrokerCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("pending broker checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedVolume(): Double = round((threshold - ceiling) * 100) / 100

    fun snapshot(): PendingBrokerCheckpoint {
        return PendingBrokerCheckpoint(
            code = code,
            name = name,
            ceiling = ceiling,
            externalRef = externalRef,
            summary = summary,
            graceDays = graceDays,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending broker checkpoint in status proposed. */
fun newPendingBrokerCheckpoint(code: String, name: String): PendingBrokerCheckpoint {
    return PendingBrokerCheckpoint(
        code = code,
        name = name,
        ceiling = 0.0,
        externalRef = "",
        summary = "",
        graceDays = 0,
        threshold = 0.0,
        status = PendingBrokerCheckpointStatus.PROPOSED,
    )
}
