package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary idling checkpoint records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a primary idling checkpoint is in its life cycle. */
enum class PrimaryIdlingCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary idling checkpoint as the idling team keeps it. */
data class PrimaryIdlingCheckpoint(
    var code: String,
    var name: String,
    var timezone: String,
    var notes: String,
    var externalRef: String,
    var discount: Double,
    var rate: Double,
    var graceDays: Int,
    var status: PrimaryIdlingCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryIdlingCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryIdlingCheckpointStatus): Boolean {
        when (status) {
            PrimaryIdlingCheckpointStatus.IDLE -> return next == PrimaryIdlingCheckpointStatus.WARMING
            PrimaryIdlingCheckpointStatus.WARMING -> {
                when (next) {
                    PrimaryIdlingCheckpointStatus.READY, PrimaryIdlingCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryIdlingCheckpointStatus.READY -> return next == PrimaryIdlingCheckpointStatus.DRAINING
            PrimaryIdlingCheckpointStatus.DRAINING -> {
                when (next) {
                    PrimaryIdlingCheckpointStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryIdlingCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("primary idling checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedValue(): Double = round((rate - discount) * 100) / 100

    fun snapshot(): PrimaryIdlingCheckpoint {
        return PrimaryIdlingCheckpoint(
            code = code,
            name = name,
            timezone = timezone,
            notes = notes,
            externalRef = externalRef,
            discount = discount,
            rate = rate,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary idling checkpoint in status idle. */
fun newPrimaryIdlingCheckpoint(code: String, name: String): PrimaryIdlingCheckpoint {
    return PrimaryIdlingCheckpoint(
        code = code,
        name = name,
        timezone = "",
        notes = "",
        externalRef = "",
        discount = 0.0,
        rate = 0.0,
        graceDays = 0,
        status = PrimaryIdlingCheckpointStatus.IDLE,
    )
}
