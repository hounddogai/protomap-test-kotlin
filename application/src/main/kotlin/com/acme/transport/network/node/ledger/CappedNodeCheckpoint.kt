package com.acme.transport.network.node.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped node checkpoint is in its life cycle. */
enum class CappedNodeCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped node checkpoint as the node team keeps it. */
data class CappedNodeCheckpoint(
    var code: String,
    var title: String,
    var category: String,
    var minimum: Double,
    var channel: String,
    var reviewer: String,
    var sequence: Int,
    var active: Boolean,
    var createdAt: Int,
    var status: CappedNodeCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedNodeCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedNodeCheckpointStatus): Boolean {
        when (status) {
            CappedNodeCheckpointStatus.PROPOSED -> {
                when (next) {
                    CappedNodeCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedNodeCheckpointStatus.APPROVED -> {
                when (next) {
                    CappedNodeCheckpointStatus.REJECTED, CappedNodeCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedNodeCheckpointStatus.REJECTED -> {
                when (next) {
                    CappedNodeCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedNodeCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("capped node checkpoint $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveTotal(): Double = round((minimum + sequence.toDouble()) * 100) / 100

    fun snapshot(): CappedNodeCheckpoint {
        return CappedNodeCheckpoint(
            code = code,
            title = title,
            category = category,
            minimum = minimum,
            channel = channel,
            reviewer = reviewer,
            sequence = sequence,
            active = active,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped node checkpoint in status proposed. */
fun newCappedNodeCheckpoint(code: String, title: String): CappedNodeCheckpoint {
    return CappedNodeCheckpoint(
        code = code,
        title = title,
        category = "",
        minimum = 0.0,
        channel = "",
        reviewer = "",
        sequence = 0,
        active = false,
        createdAt = 0,
        status = CappedNodeCheckpointStatus.PROPOSED,
    )
}
