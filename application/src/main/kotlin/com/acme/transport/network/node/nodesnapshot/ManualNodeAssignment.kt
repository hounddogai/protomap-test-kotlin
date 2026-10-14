package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual node assignment is in its life cycle. */
enum class ManualNodeAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One manual node assignment, with what the planners need to act on it. */
data class ManualNodeAssignment(
    var code: String,
    var label: String,
    var cost: Double,
    var weight: Double,
    var margin: Double,
    var attributes: MutableMap<String, String>,
    var approved: Boolean,
    var capacity: Int,
    var lane: String,
    var status: ManualNodeAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualNodeAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualNodeAssignmentStatus): Boolean {
        when (status) {
            ManualNodeAssignmentStatus.PROPOSED -> {
                when (next) {
                    ManualNodeAssignmentStatus.APPROVED, ManualNodeAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualNodeAssignmentStatus.APPROVED -> return next == ManualNodeAssignmentStatus.WITHDRAWN
            ManualNodeAssignmentStatus.REJECTED -> {
                when (next) {
                    ManualNodeAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualNodeAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("manual node assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual node assignment"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round((capacity.toDouble() + cost) * 100) / 100

    fun snapshot(): ManualNodeAssignment {
        return ManualNodeAssignment(
            code = code,
            label = label,
            cost = cost,
            weight = weight,
            margin = margin,
            attributes = attributes,
            approved = approved,
            capacity = capacity,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual node assignment in status proposed. */
fun newManualNodeAssignment(code: String, label: String): ManualNodeAssignment {
    return ManualNodeAssignment(
        code = code,
        label = label,
        cost = 0.0,
        weight = 0.0,
        margin = 0.0,
        attributes = mutableMapOf<String, String>(),
        approved = false,
        capacity = 0,
        lane = "",
        status = ManualNodeAssignmentStatus.PROPOSED,
    )
}
