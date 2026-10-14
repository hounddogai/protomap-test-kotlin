package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.text.normalize

// The global violation segment records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a global violation segment is in its life cycle. */
enum class GlobalViolationSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global violation segment as the violation team keeps it. */
data class GlobalViolationSegment(
    var code: String,
    var title: String,
    var currency: String,
    var contactEmail: String,
    var depot: String,
    var summary: String,
    var ownerId: String,
    var score: Double,
    var status: GlobalViolationSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalViolationSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalViolationSegmentStatus): Boolean {
        when (status) {
            GlobalViolationSegmentStatus.PROPOSED -> {
                when (next) {
                    GlobalViolationSegmentStatus.REJECTED, GlobalViolationSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalViolationSegmentStatus.APPROVED -> {
                when (next) {
                    GlobalViolationSegmentStatus.REJECTED, GlobalViolationSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalViolationSegmentStatus.REJECTED -> {
                when (next) {
                    GlobalViolationSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalViolationSegmentStatus) {
        if (!canMoveTo(next)) {
            error("global violation segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global violation segment"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): GlobalViolationSegment {
        return GlobalViolationSegment(
            code = code,
            title = title,
            currency = currency,
            contactEmail = contactEmail,
            depot = depot,
            summary = summary,
            ownerId = ownerId,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global violation segment in status proposed. */
fun newGlobalViolationSegment(code: String, title: String): GlobalViolationSegment {
    return GlobalViolationSegment(
        code = code,
        title = title,
        currency = "",
        contactEmail = "",
        depot = "",
        summary = "",
        ownerId = "",
        score = 0.0,
        status = GlobalViolationSegmentStatus.PROPOSED,
    )
}
