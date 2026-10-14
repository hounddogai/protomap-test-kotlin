package com.acme.transport.freightpay.accessorial.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial checkpoint is in its life cycle. */
enum class AccessorialCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a accessorial checkpoint and its current status. */
data class AccessorialCheckpoint(
    var code: String,
    var name: String,
    var ceiling: Double,
    var version: Int,
    var active: Boolean,
    var status: AccessorialCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialCheckpointStatus): Boolean {
        when (status) {
            AccessorialCheckpointStatus.IDLE -> {
                when (next) {
                    AccessorialCheckpointStatus.WARMING -> return true
                }
                return false
            }
            AccessorialCheckpointStatus.WARMING -> return next == AccessorialCheckpointStatus.READY || next == AccessorialCheckpointStatus.STOPPED
            AccessorialCheckpointStatus.READY -> {
                when (next) {
                    AccessorialCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            AccessorialCheckpointStatus.DRAINING -> return next == AccessorialCheckpointStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AccessorialCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("accessorial checkpoint $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun projectedValue(): Double = round(version.toDouble() * ceiling * 100) / 100

    fun snapshot(): AccessorialCheckpoint {
        return AccessorialCheckpoint(
            code = code,
            name = name,
            ceiling = ceiling,
            version = version,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a accessorial checkpoint in status idle. */
fun newAccessorialCheckpoint(code: String, name: String): AccessorialCheckpoint {
    return AccessorialCheckpoint(
        code = code,
        name = name,
        ceiling = 0.0,
        version = 0,
        active = false,
        status = AccessorialCheckpointStatus.IDLE,
    )
}
