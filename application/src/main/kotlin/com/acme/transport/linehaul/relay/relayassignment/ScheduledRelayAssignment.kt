package com.acme.transport.linehaul.relay.relayassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled relay assignment records of linehaul's relay work, and the rules, stores, and calculations
// around them.

/** Where a scheduled relay assignment is in its life cycle. */
enum class ScheduledRelayAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a scheduled relay assignment and its current status. */
data class ScheduledRelayAssignment(
    var code: String,
    var title: String,
    var sequence: Int,
    var lane: String,
    var expiresAt: Int,
    var tags: MutableList<String>,
    var channel: String,
    var vehicleId: String,
    var timezone: String,
    var status: ScheduledRelayAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRelayAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRelayAssignmentStatus): Boolean {
        when (status) {
            ScheduledRelayAssignmentStatus.DRAFT -> return next == ScheduledRelayAssignmentStatus.PENDING || next == ScheduledRelayAssignmentStatus.SUSPENDED
            ScheduledRelayAssignmentStatus.PENDING -> {
                when (next) {
                    ScheduledRelayAssignmentStatus.ACTIVE, ScheduledRelayAssignmentStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledRelayAssignmentStatus.ACTIVE -> return next == ScheduledRelayAssignmentStatus.SUSPENDED || next == ScheduledRelayAssignmentStatus.CLOSED
            ScheduledRelayAssignmentStatus.SUSPENDED -> {
                when (next) {
                    ScheduledRelayAssignmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRelayAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("scheduled relay assignment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round((sequence.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledRelayAssignment {
        return ScheduledRelayAssignment(
            code = code,
            title = title,
            sequence = sequence,
            lane = lane,
            expiresAt = expiresAt,
            tags = tags,
            channel = channel,
            vehicleId = vehicleId,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled relay assignment in status draft. */
fun newScheduledRelayAssignment(code: String, title: String): ScheduledRelayAssignment {
    return ScheduledRelayAssignment(
        code = code,
        title = title,
        sequence = 0,
        lane = "",
        expiresAt = 0,
        tags = mutableListOf<String>(),
        channel = "",
        vehicleId = "",
        timezone = "",
        status = ScheduledRelayAssignmentStatus.DRAFT,
    )
}
