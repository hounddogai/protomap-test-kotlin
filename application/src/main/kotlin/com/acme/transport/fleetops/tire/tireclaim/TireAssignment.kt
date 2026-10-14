package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.kit.text.normalize

/** Where a tire assignment is in its life cycle. */
enum class TireAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tire assignment as the tire team keeps it. */
data class TireAssignment(
    var code: String,
    var title: String,
    var name: String,
    var region: String,
    var durationMinutes: Int,
    var description: String,
    var source: String,
    var status: TireAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireAssignmentStatus): Boolean {
        when (status) {
            TireAssignmentStatus.PLANNED -> {
                when (next) {
                    TireAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            TireAssignmentStatus.SCHEDULED -> return next == TireAssignmentStatus.IN_PROGRESS || next == TireAssignmentStatus.CANCELLED
            TireAssignmentStatus.IN_PROGRESS -> {
                when (next) {
                    TireAssignmentStatus.COMPLETED, TireAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            TireAssignmentStatus.COMPLETED -> return next == TireAssignmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TireAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("tire assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): TireAssignment {
        return TireAssignment(
            code = code,
            title = title,
            name = name,
            region = region,
            durationMinutes = durationMinutes,
            description = description,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tire assignment in status planned. */
fun newTireAssignment(code: String, title: String): TireAssignment {
    return TireAssignment(
        code = code,
        title = title,
        name = "",
        region = "",
        durationMinutes = 0,
        description = "",
        source = "",
        status = TireAssignmentStatus.PLANNED,
    )
}
