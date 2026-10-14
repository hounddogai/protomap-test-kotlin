package com.acme.transport.safety.incident.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident assignment is in its life cycle. */
enum class IncidentAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A incident assignment as the incident team keeps it. */
data class IncidentAssignment(
    var code: String,
    var title: String,
    var batchSize: Int,
    var revision: Int,
    var margin: Double,
    var maximum: Double,
    var carrier: String,
    var pinned: Boolean,
    var visible: Boolean,
    var status: IncidentAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentAssignmentStatus): Boolean {
        when (status) {
            IncidentAssignmentStatus.PLANNED -> return next == IncidentAssignmentStatus.IN_PROGRESS || next == IncidentAssignmentStatus.CANCELLED
            IncidentAssignmentStatus.SCHEDULED -> {
                when (next) {
                    IncidentAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            IncidentAssignmentStatus.IN_PROGRESS -> {
                when (next) {
                    IncidentAssignmentStatus.COMPLETED, IncidentAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            IncidentAssignmentStatus.COMPLETED -> return next == IncidentAssignmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: IncidentAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("incident assignment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round((margin + revision.toDouble()) * 100) / 100

    fun snapshot(): IncidentAssignment {
        return IncidentAssignment(
            code = code,
            title = title,
            batchSize = batchSize,
            revision = revision,
            margin = margin,
            maximum = maximum,
            carrier = carrier,
            pinned = pinned,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a incident assignment in status planned. */
fun newIncidentAssignment(code: String, title: String): IncidentAssignment {
    return IncidentAssignment(
        code = code,
        title = title,
        batchSize = 0,
        revision = 0,
        margin = 0.0,
        maximum = 0.0,
        carrier = "",
        pinned = false,
        visible = false,
        status = IncidentAssignmentStatus.PLANNED,
    )
}
