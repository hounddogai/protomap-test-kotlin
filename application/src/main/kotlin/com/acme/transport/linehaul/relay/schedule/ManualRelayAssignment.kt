package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.kit.text.normalize

/** Where a manual relay assignment is in its life cycle. */
enum class ManualRelayAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual relay assignment. Its code is unique within a tenant. */
data class ManualRelayAssignment(
    var code: String,
    var title: String,
    var archived: Boolean,
    var loadKg: Double,
    var region: String,
    var status: ManualRelayAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRelayAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRelayAssignmentStatus): Boolean {
        when (status) {
            ManualRelayAssignmentStatus.CREATED -> return next == ManualRelayAssignmentStatus.VALIDATED || next == ManualRelayAssignmentStatus.RETIRED
            ManualRelayAssignmentStatus.VALIDATED -> {
                when (next) {
                    ManualRelayAssignmentStatus.PUBLISHED -> return true
                }
                return false
            }
            ManualRelayAssignmentStatus.PUBLISHED -> return next == ManualRelayAssignmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualRelayAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("manual relay assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = loadKg

    fun snapshot(): ManualRelayAssignment {
        return ManualRelayAssignment(
            code = code,
            title = title,
            archived = archived,
            loadKg = loadKg,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a manual relay assignment in status created. */
fun newManualRelayAssignment(code: String, title: String): ManualRelayAssignment {
    return ManualRelayAssignment(
        code = code,
        title = title,
        archived = false,
        loadKg = 0.0,
        region = "",
        status = ManualRelayAssignmentStatus.CREATED,
    )
}
