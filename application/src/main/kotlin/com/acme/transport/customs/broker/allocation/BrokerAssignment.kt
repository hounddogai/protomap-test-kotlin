package com.acme.transport.customs.broker.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker assignment is in its life cycle. */
enum class BrokerAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a broker assignment and its current status. */
data class BrokerAssignment(
    var code: String,
    var title: String,
    var score: Double,
    var endsAt: Int,
    var contactEmail: String,
    var unit: String,
    var status: BrokerAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerAssignmentStatus): Boolean {
        when (status) {
            BrokerAssignmentStatus.OPEN -> {
                when (next) {
                    BrokerAssignmentStatus.IN_REVIEW, BrokerAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            BrokerAssignmentStatus.IN_REVIEW -> return next == BrokerAssignmentStatus.REOPENED || next == BrokerAssignmentStatus.ARCHIVED
            BrokerAssignmentStatus.RESOLVED -> {
                when (next) {
                    BrokerAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            BrokerAssignmentStatus.REOPENED -> {
                when (next) {
                    BrokerAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("broker assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedExposure(): Double = round(endsAt.toDouble() * score * 100) / 100

    fun snapshot(): BrokerAssignment {
        return BrokerAssignment(
            code = code,
            title = title,
            score = score,
            endsAt = endsAt,
            contactEmail = contactEmail,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a broker assignment in status open. */
fun newBrokerAssignment(code: String, title: String): BrokerAssignment {
    return BrokerAssignment(
        code = code,
        title = title,
        score = 0.0,
        endsAt = 0,
        contactEmail = "",
        unit = "",
        status = BrokerAssignmentStatus.OPEN,
    )
}
