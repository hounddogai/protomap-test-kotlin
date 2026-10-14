package com.acme.transport.linehaul.laneplan.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lane plan checkpoint records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a lane plan checkpoint is in its life cycle. */
enum class LanePlanCheckpointStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lane plan checkpoint. Its code is unique within a tenant. */
data class LanePlanCheckpoint(
    var code: String,
    var label: String,
    var stopCount: Int,
    var segment: String,
    var attributes: MutableMap<String, String>,
    var approved: Boolean,
    var margin: Double,
    var distanceKm: Double,
    var category: String,
    var updatedAt: Int,
    var status: LanePlanCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanCheckpointStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanCheckpointStatus): Boolean {
        when (status) {
            LanePlanCheckpointStatus.NEW -> {
                when (next) {
                    LanePlanCheckpointStatus.RUNNING, LanePlanCheckpointStatus.FAILED -> return true
                }
                return false
            }
            LanePlanCheckpointStatus.QUEUED -> return next == LanePlanCheckpointStatus.RUNNING
            LanePlanCheckpointStatus.RUNNING -> {
                when (next) {
                    LanePlanCheckpointStatus.DONE, LanePlanCheckpointStatus.FAILED -> return true
                }
                return false
            }
            LanePlanCheckpointStatus.DONE -> {
                when (next) {
                    LanePlanCheckpointStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("lane plan checkpoint $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"lane plan checkpoint"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round(distanceKm * stopCount.toDouble() * 100) / 100

    fun snapshot(): LanePlanCheckpoint {
        return LanePlanCheckpoint(
            code = code,
            label = label,
            stopCount = stopCount,
            segment = segment,
            attributes = attributes,
            approved = approved,
            margin = margin,
            distanceKm = distanceKm,
            category = category,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a lane plan checkpoint in status new. */
fun newLanePlanCheckpoint(code: String, label: String): LanePlanCheckpoint {
    return LanePlanCheckpoint(
        code = code,
        label = label,
        stopCount = 0,
        segment = "",
        attributes = mutableMapOf<String, String>(),
        approved = false,
        margin = 0.0,
        distanceKm = 0.0,
        category = "",
        updatedAt = 0,
        status = LanePlanCheckpointStatus.NEW,
    )
}
