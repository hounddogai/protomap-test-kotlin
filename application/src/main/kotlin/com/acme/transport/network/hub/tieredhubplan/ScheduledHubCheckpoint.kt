package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled hub checkpoint is in its life cycle. */
enum class ScheduledHubCheckpointStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One scheduled hub checkpoint, with what the planners need to act on it. */
data class ScheduledHubCheckpoint(
    var code: String,
    var name: String,
    var discount: Double,
    var fuelLiters: Double,
    var rate: Double,
    var label: String,
    var ratio: Double,
    var floorValue: Double,
    var sequence: Int,
    var status: ScheduledHubCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHubCheckpointStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHubCheckpointStatus): Boolean {
        when (status) {
            ScheduledHubCheckpointStatus.NEW -> {
                when (next) {
                    ScheduledHubCheckpointStatus.QUEUED, ScheduledHubCheckpointStatus.DONE -> return true
                }
                return false
            }
            ScheduledHubCheckpointStatus.QUEUED -> {
                when (next) {
                    ScheduledHubCheckpointStatus.FAILED -> return true
                }
                return false
            }
            ScheduledHubCheckpointStatus.RUNNING -> {
                when (next) {
                    ScheduledHubCheckpointStatus.DONE, ScheduledHubCheckpointStatus.FAILED -> return true
                }
                return false
            }
            ScheduledHubCheckpointStatus.DONE -> return next == ScheduledHubCheckpointStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledHubCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hub checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun adjustedValue(): Double = round((ratio + discount) * 100) / 100

    fun snapshot(): ScheduledHubCheckpoint {
        return ScheduledHubCheckpoint(
            code = code,
            name = name,
            discount = discount,
            fuelLiters = fuelLiters,
            rate = rate,
            label = label,
            ratio = ratio,
            floorValue = floorValue,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled hub checkpoint in status new. */
fun newScheduledHubCheckpoint(code: String, name: String): ScheduledHubCheckpoint {
    return ScheduledHubCheckpoint(
        code = code,
        name = name,
        discount = 0.0,
        fuelLiters = 0.0,
        rate = 0.0,
        label = "",
        ratio = 0.0,
        floorValue = 0.0,
        sequence = 0,
        status = ScheduledHubCheckpointStatus.NEW,
    )
}
