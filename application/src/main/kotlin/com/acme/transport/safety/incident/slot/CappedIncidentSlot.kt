package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped incident slot records of safety's incident work, and the rules, stores, and calculations around
// them.

/** Where a capped incident slot is in its life cycle. */
enum class CappedIncidentSlotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped incident slot as the incident team keeps it. */
data class CappedIncidentSlot(
    var code: String,
    var title: String,
    var margin: Double,
    var contactEmail: String,
    var capacity: Int,
    var attempts: Int,
    var stopCount: Int,
    var score: Double,
    var status: CappedIncidentSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedIncidentSlotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedIncidentSlotStatus): Boolean {
        when (status) {
            CappedIncidentSlotStatus.DRAFT -> return next == CappedIncidentSlotStatus.ACTIVE
            CappedIncidentSlotStatus.PENDING -> return next == CappedIncidentSlotStatus.SUSPENDED
            CappedIncidentSlotStatus.ACTIVE -> return next == CappedIncidentSlotStatus.SUSPENDED || next == CappedIncidentSlotStatus.CLOSED
            CappedIncidentSlotStatus.SUSPENDED -> return next == CappedIncidentSlotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedIncidentSlotStatus) {
        if (!canMoveTo(next)) {
            error("capped incident slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped incident slot"} $code"

    fun effectiveLoad(): Double = round(attempts.toDouble() * score * 100) / 100

    fun snapshot(): CappedIncidentSlot {
        return CappedIncidentSlot(
            code = code,
            title = title,
            margin = margin,
            contactEmail = contactEmail,
            capacity = capacity,
            attempts = attempts,
            stopCount = stopCount,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped incident slot in status draft. */
fun newCappedIncidentSlot(code: String, title: String): CappedIncidentSlot {
    return CappedIncidentSlot(
        code = code,
        title = title,
        margin = 0.0,
        contactEmail = "",
        capacity = 0,
        attempts = 0,
        stopCount = 0,
        score = 0.0,
        status = CappedIncidentSlotStatus.DRAFT,
    )
}
