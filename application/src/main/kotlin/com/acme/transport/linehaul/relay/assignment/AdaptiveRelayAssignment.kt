package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.text.normalize

/** Where a adaptive relay assignment is in its life cycle. */
enum class AdaptiveRelayAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive relay assignment as the relay team keeps it. */
data class AdaptiveRelayAssignment(
    var code: String,
    var title: String,
    var routeCode: String,
    var contactEmail: String,
    var depot: String,
    var source: String,
    var pinned: Boolean,
    var visible: Boolean,
    var loadKg: Double,
    var reviewer: String,
    var status: AdaptiveRelayAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRelayAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRelayAssignmentStatus): Boolean {
        when (status) {
            AdaptiveRelayAssignmentStatus.PLANNED -> {
                when (next) {
                    AdaptiveRelayAssignmentStatus.COMPLETED -> return true
                }
                return false
            }
            AdaptiveRelayAssignmentStatus.SCHEDULED -> return next == AdaptiveRelayAssignmentStatus.COMPLETED || next == AdaptiveRelayAssignmentStatus.CANCELLED
            AdaptiveRelayAssignmentStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveRelayAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveRelayAssignmentStatus.COMPLETED -> {
                when (next) {
                    AdaptiveRelayAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveRelayAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("adaptive relay assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = loadKg

    fun snapshot(): AdaptiveRelayAssignment {
        return AdaptiveRelayAssignment(
            code = code,
            title = title,
            routeCode = routeCode,
            contactEmail = contactEmail,
            depot = depot,
            source = source,
            pinned = pinned,
            visible = visible,
            loadKg = loadKg,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a adaptive relay assignment in status planned. */
fun newAdaptiveRelayAssignment(code: String, title: String): AdaptiveRelayAssignment {
    return AdaptiveRelayAssignment(
        code = code,
        title = title,
        routeCode = "",
        contactEmail = "",
        depot = "",
        source = "",
        pinned = false,
        visible = false,
        loadKg = 0.0,
        reviewer = "",
        status = AdaptiveRelayAssignmentStatus.PLANNED,
    )
}
