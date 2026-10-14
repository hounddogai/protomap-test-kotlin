package com.acme.transport.crewing.restbreak.breakmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict rest break checkpoint is in its life cycle. */
enum class StrictRestBreakCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One strict rest break checkpoint, with what the planners need to act on it. */
data class StrictRestBreakCheckpoint(
    var code: String,
    var name: String,
    var version: Int,
    var windowDays: Int,
    var phone: String,
    var minimum: Double,
    var status: StrictRestBreakCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRestBreakCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRestBreakCheckpointStatus): Boolean {
        when (status) {
            StrictRestBreakCheckpointStatus.DRAFT -> {
                when (next) {
                    StrictRestBreakCheckpointStatus.CLOSED -> return true
                }
                return false
            }
            StrictRestBreakCheckpointStatus.PENDING -> {
                when (next) {
                    StrictRestBreakCheckpointStatus.ACTIVE, StrictRestBreakCheckpointStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictRestBreakCheckpointStatus.ACTIVE -> {
                when (next) {
                    StrictRestBreakCheckpointStatus.SUSPENDED, StrictRestBreakCheckpointStatus.CLOSED -> return true
                }
                return false
            }
            StrictRestBreakCheckpointStatus.SUSPENDED -> {
                when (next) {
                    StrictRestBreakCheckpointStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictRestBreakCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("strict rest break checkpoint $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun projectedTotal(): Double = round(version.toDouble() * minimum * 100) / 100

    fun snapshot(): StrictRestBreakCheckpoint {
        return StrictRestBreakCheckpoint(
            code = code,
            name = name,
            version = version,
            windowDays = windowDays,
            phone = phone,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict rest break checkpoint in status draft. */
fun newStrictRestBreakCheckpoint(code: String, name: String): StrictRestBreakCheckpoint {
    return StrictRestBreakCheckpoint(
        code = code,
        name = name,
        version = 0,
        windowDays = 0,
        phone = "",
        minimum = 0.0,
        status = StrictRestBreakCheckpointStatus.DRAFT,
    )
}
