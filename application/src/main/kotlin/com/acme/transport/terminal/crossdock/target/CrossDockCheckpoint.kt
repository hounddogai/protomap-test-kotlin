package com.acme.transport.terminal.crossdock.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock checkpoint is in its life cycle. */
enum class CrossDockCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A cross dock checkpoint as the cross dock team keeps it. */
data class CrossDockCheckpoint(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var ceiling: Double,
    var contactEmail: String,
    var status: CrossDockCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockCheckpointStatus): Boolean {
        when (status) {
            CrossDockCheckpointStatus.PROPOSED -> {
                when (next) {
                    CrossDockCheckpointStatus.REJECTED -> return true
                }
                return false
            }
            CrossDockCheckpointStatus.APPROVED -> {
                when (next) {
                    CrossDockCheckpointStatus.REJECTED, CrossDockCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
            CrossDockCheckpointStatus.REJECTED -> {
                when (next) {
                    CrossDockCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("cross dock checkpoint $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedValue(): Double = round(updatedAt.toDouble() * ceiling * 100) / 100

    fun snapshot(): CrossDockCheckpoint {
        return CrossDockCheckpoint(
            code = code,
            label = label,
            updatedAt = updatedAt,
            ceiling = ceiling,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a cross dock checkpoint in status proposed. */
fun newCrossDockCheckpoint(code: String, label: String): CrossDockCheckpoint {
    return CrossDockCheckpoint(
        code = code,
        label = label,
        updatedAt = 0,
        ceiling = 0.0,
        contactEmail = "",
        status = CrossDockCheckpointStatus.PROPOSED,
    )
}
