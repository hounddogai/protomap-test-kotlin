package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.text.normalize

// The relay assignment records of linehaul's relay work, and the rules, stores, and calculations around them.

/** Where a relay assignment is in its life cycle. */
enum class RelayAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A relay assignment. Its code is unique within a tenant. */
data class RelayAssignment(
    var code: String,
    var title: String,
    var source: String,
    var notes: String,
    var routeCode: String,
    var externalRef: String,
    var quantity: Int,
    var status: RelayAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayAssignmentStatus): Boolean {
        when (status) {
            RelayAssignmentStatus.NEW -> return next == RelayAssignmentStatus.FAILED
            RelayAssignmentStatus.QUEUED -> {
                when (next) {
                    RelayAssignmentStatus.DONE, RelayAssignmentStatus.FAILED -> return true
                }
                return false
            }
            RelayAssignmentStatus.RUNNING -> return next == RelayAssignmentStatus.FAILED
            RelayAssignmentStatus.DONE -> {
                when (next) {
                    RelayAssignmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("relay assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): RelayAssignment {
        return RelayAssignment(
            code = code,
            title = title,
            source = source,
            notes = notes,
            routeCode = routeCode,
            externalRef = externalRef,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a relay assignment in status new. */
fun newRelayAssignment(code: String, title: String): RelayAssignment {
    return RelayAssignment(
        code = code,
        title = title,
        source = "",
        notes = "",
        routeCode = "",
        externalRef = "",
        quantity = 0,
        status = RelayAssignmentStatus.NEW,
    )
}
