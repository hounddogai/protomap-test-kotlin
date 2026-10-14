package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.text.normalize

// The primary incident segment records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a primary incident segment is in its life cycle. */
enum class PrimaryIncidentSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a primary incident segment and its current status. */
data class PrimaryIncidentSegment(
    var code: String,
    var title: String,
    var visible: Boolean,
    var dock: String,
    var attributes: MutableMap<String, String>,
    var amount: Double,
    var status: PrimaryIncidentSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryIncidentSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryIncidentSegmentStatus): Boolean {
        when (status) {
            PrimaryIncidentSegmentStatus.PLANNED -> return next == PrimaryIncidentSegmentStatus.SCHEDULED
            PrimaryIncidentSegmentStatus.SCHEDULED -> {
                when (next) {
                    PrimaryIncidentSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryIncidentSegmentStatus.IN_PROGRESS -> {
                when (next) {
                    PrimaryIncidentSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryIncidentSegmentStatus.COMPLETED -> {
                when (next) {
                    PrimaryIncidentSegmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryIncidentSegmentStatus) {
        if (!canMoveTo(next)) {
            error("primary incident segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary incident segment"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = amount

    fun snapshot(): PrimaryIncidentSegment {
        return PrimaryIncidentSegment(
            code = code,
            title = title,
            visible = visible,
            dock = dock,
            attributes = attributes,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary incident segment in status planned. */
fun newPrimaryIncidentSegment(code: String, title: String): PrimaryIncidentSegment {
    return PrimaryIncidentSegment(
        code = code,
        title = title,
        visible = false,
        dock = "",
        attributes = mutableMapOf<String, String>(),
        amount = 0.0,
        status = PrimaryIncidentSegmentStatus.PLANNED,
    )
}
