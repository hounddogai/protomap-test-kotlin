package com.acme.transport.customs.broker.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped broker checkpoint records of customs's broker work, and the rules, stores, and calculations
// around them.

/** Where a capped broker checkpoint is in its life cycle. */
enum class CappedBrokerCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped broker checkpoint. Its code is unique within a tenant. */
data class CappedBrokerCheckpoint(
    var code: String,
    var name: String,
    var discount: Double,
    var priority: Int,
    var summary: String,
    var createdAt: Int,
    var status: CappedBrokerCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedBrokerCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedBrokerCheckpointStatus): Boolean {
        when (status) {
            CappedBrokerCheckpointStatus.DRAFT -> return next == CappedBrokerCheckpointStatus.PENDING || next == CappedBrokerCheckpointStatus.CLOSED
            CappedBrokerCheckpointStatus.PENDING -> return next == CappedBrokerCheckpointStatus.ACTIVE
            CappedBrokerCheckpointStatus.ACTIVE -> return next == CappedBrokerCheckpointStatus.CLOSED
            CappedBrokerCheckpointStatus.SUSPENDED -> {
                when (next) {
                    CappedBrokerCheckpointStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedBrokerCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("capped broker checkpoint $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netLoad(): Double = round((createdAt.toDouble() + discount) * 100) / 100

    fun snapshot(): CappedBrokerCheckpoint {
        return CappedBrokerCheckpoint(
            code = code,
            name = name,
            discount = discount,
            priority = priority,
            summary = summary,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped broker checkpoint in status draft. */
fun newCappedBrokerCheckpoint(code: String, name: String): CappedBrokerCheckpoint {
    return CappedBrokerCheckpoint(
        code = code,
        name = name,
        discount = 0.0,
        priority = 0,
        summary = "",
        createdAt = 0,
        status = CappedBrokerCheckpointStatus.DRAFT,
    )
}
