package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.kit.text.normalize

/** Where a dock door policy is in its life cycle. */
enum class DockDoorPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a dock door policy and its current status. */
data class DockDoorPolicy(
    var code: String,
    var name: String,
    var depot: String,
    var dock: String,
    var threshold: Double,
    var timezone: String,
    var status: DockDoorPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorPolicyStatus): Boolean {
        when (status) {
            DockDoorPolicyStatus.REQUESTED -> return next == DockDoorPolicyStatus.GRANTED || next == DockDoorPolicyStatus.EXPIRED
            DockDoorPolicyStatus.GRANTED -> return next == DockDoorPolicyStatus.EXPIRED
            DockDoorPolicyStatus.REVOKED -> return next == DockDoorPolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DockDoorPolicyStatus) {
        if (!canMoveTo(next)) {
            error("dock door policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = threshold

    fun snapshot(): DockDoorPolicy {
        return DockDoorPolicy(
            code = code,
            name = name,
            depot = depot,
            dock = dock,
            threshold = threshold,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a dock door policy in status requested. */
fun newDockDoorPolicy(code: String, name: String): DockDoorPolicy {
    return DockDoorPolicy(
        code = code,
        name = name,
        depot = "",
        dock = "",
        threshold = 0.0,
        timezone = "",
        status = DockDoorPolicyStatus.REQUESTED,
    )
}
