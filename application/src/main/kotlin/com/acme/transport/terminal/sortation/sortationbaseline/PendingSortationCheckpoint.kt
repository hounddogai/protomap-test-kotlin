package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.text.normalize

/** Where a pending sortation checkpoint is in its life cycle. */
enum class PendingSortationCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending sortation checkpoint. Its code is unique within a tenant. */
data class PendingSortationCheckpoint(
    var code: String,
    var label: String,
    var limit: Int,
    var externalRef: String,
    var shiftCode: String,
    var status: PendingSortationCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingSortationCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingSortationCheckpointStatus): Boolean {
        when (status) {
            PendingSortationCheckpointStatus.REQUESTED -> {
                when (next) {
                    PendingSortationCheckpointStatus.GRANTED, PendingSortationCheckpointStatus.REVOKED -> return true
                }
                return false
            }
            PendingSortationCheckpointStatus.GRANTED -> {
                when (next) {
                    PendingSortationCheckpointStatus.REVOKED, PendingSortationCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
            PendingSortationCheckpointStatus.REVOKED -> {
                when (next) {
                    PendingSortationCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingSortationCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("pending sortation checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending sortation checkpoint"} $code"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): PendingSortationCheckpoint {
        return PendingSortationCheckpoint(
            code = code,
            label = label,
            limit = limit,
            externalRef = externalRef,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending sortation checkpoint in status requested. */
fun newPendingSortationCheckpoint(code: String, label: String): PendingSortationCheckpoint {
    return PendingSortationCheckpoint(
        code = code,
        label = label,
        limit = 0,
        externalRef = "",
        shiftCode = "",
        status = PendingSortationCheckpointStatus.REQUESTED,
    )
}
