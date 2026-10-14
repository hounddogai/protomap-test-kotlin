package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted camera event assignment is in its life cycle. */
enum class WeightedCameraEventAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted camera event assignment. Its code is unique within a tenant. */
data class WeightedCameraEventAssignment(
    var code: String,
    var label: String,
    var carrier: String,
    var tags: MutableList<String>,
    var ratio: Double,
    var loadKg: Double,
    var status: WeightedCameraEventAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCameraEventAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCameraEventAssignmentStatus): Boolean {
        when (status) {
            WeightedCameraEventAssignmentStatus.REQUESTED -> {
                when (next) {
                    WeightedCameraEventAssignmentStatus.GRANTED, WeightedCameraEventAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedCameraEventAssignmentStatus.GRANTED -> {
                when (next) {
                    WeightedCameraEventAssignmentStatus.REVOKED, WeightedCameraEventAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedCameraEventAssignmentStatus.REVOKED -> {
                when (next) {
                    WeightedCameraEventAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedCameraEventAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("weighted camera event assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round((loadKg - ratio) * 100) / 100

    fun snapshot(): WeightedCameraEventAssignment {
        return WeightedCameraEventAssignment(
            code = code,
            label = label,
            carrier = carrier,
            tags = tags,
            ratio = ratio,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted camera event assignment in status requested. */
fun newWeightedCameraEventAssignment(code: String, label: String): WeightedCameraEventAssignment {
    return WeightedCameraEventAssignment(
        code = code,
        label = label,
        carrier = "",
        tags = mutableListOf<String>(),
        ratio = 0.0,
        loadKg = 0.0,
        status = WeightedCameraEventAssignmentStatus.REQUESTED,
    )
}
