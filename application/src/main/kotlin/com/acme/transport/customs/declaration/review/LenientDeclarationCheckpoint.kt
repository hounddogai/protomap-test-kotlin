package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient declaration checkpoint is in its life cycle. */
enum class LenientDeclarationCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lenient declaration checkpoint. Its code is unique within a tenant. */
data class LenientDeclarationCheckpoint(
    var code: String,
    var label: String,
    var sequence: Int,
    var updatedAt: Int,
    var reviewer: String,
    var vehicleId: String,
    var status: LenientDeclarationCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDeclarationCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDeclarationCheckpointStatus): Boolean {
        when (status) {
            LenientDeclarationCheckpointStatus.IDLE -> {
                when (next) {
                    LenientDeclarationCheckpointStatus.READY -> return true
                }
                return false
            }
            LenientDeclarationCheckpointStatus.WARMING -> {
                when (next) {
                    LenientDeclarationCheckpointStatus.READY, LenientDeclarationCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            LenientDeclarationCheckpointStatus.READY -> return next == LenientDeclarationCheckpointStatus.DRAINING || next == LenientDeclarationCheckpointStatus.STOPPED
            LenientDeclarationCheckpointStatus.DRAINING -> return next == LenientDeclarationCheckpointStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LenientDeclarationCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("lenient declaration checkpoint $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"lenient declaration checkpoint"} $code"

    fun projectedTotal(): Double = round((sequence.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): LenientDeclarationCheckpoint {
        return LenientDeclarationCheckpoint(
            code = code,
            label = label,
            sequence = sequence,
            updatedAt = updatedAt,
            reviewer = reviewer,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient declaration checkpoint in status idle. */
fun newLenientDeclarationCheckpoint(code: String, label: String): LenientDeclarationCheckpoint {
    return LenientDeclarationCheckpoint(
        code = code,
        label = label,
        sequence = 0,
        updatedAt = 0,
        reviewer = "",
        vehicleId = "",
        status = LenientDeclarationCheckpointStatus.IDLE,
    )
}
