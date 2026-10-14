package com.acme.transport.emissions.idling.idlingcheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback idling checkpoint records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a fallback idling checkpoint is in its life cycle. */
enum class FallbackIdlingCheckpointStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One fallback idling checkpoint, with what the planners need to act on it. */
data class FallbackIdlingCheckpoint(
    var code: String,
    var name: String,
    var description: String,
    var externalRef: String,
    var attributes: MutableMap<String, String>,
    var rate: Double,
    var region: String,
    var timezone: String,
    var limit: Int,
    var floorValue: Double,
    var notes: String,
    var status: FallbackIdlingCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackIdlingCheckpointStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackIdlingCheckpointStatus): Boolean {
        when (status) {
            FallbackIdlingCheckpointStatus.NEW -> {
                when (next) {
                    FallbackIdlingCheckpointStatus.RUNNING, FallbackIdlingCheckpointStatus.FAILED -> return true
                }
                return false
            }
            FallbackIdlingCheckpointStatus.QUEUED -> return next == FallbackIdlingCheckpointStatus.DONE
            FallbackIdlingCheckpointStatus.RUNNING -> return next == FallbackIdlingCheckpointStatus.DONE
            FallbackIdlingCheckpointStatus.DONE -> return next == FallbackIdlingCheckpointStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackIdlingCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("fallback idling checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback idling checkpoint"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedValue(): Double = round((limit.toDouble() - rate) * 100) / 100

    fun snapshot(): FallbackIdlingCheckpoint {
        return FallbackIdlingCheckpoint(
            code = code,
            name = name,
            description = description,
            externalRef = externalRef,
            attributes = attributes,
            rate = rate,
            region = region,
            timezone = timezone,
            limit = limit,
            floorValue = floorValue,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback idling checkpoint in status new. */
fun newFallbackIdlingCheckpoint(code: String, name: String): FallbackIdlingCheckpoint {
    return FallbackIdlingCheckpoint(
        code = code,
        name = name,
        description = "",
        externalRef = "",
        attributes = mutableMapOf<String, String>(),
        rate = 0.0,
        region = "",
        timezone = "",
        limit = 0,
        floorValue = 0.0,
        notes = "",
        status = FallbackIdlingCheckpointStatus.NEW,
    )
}
