package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.kit.text.normalize

// The adaptive qualification assignment records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a adaptive qualification assignment is in its life cycle. */
enum class AdaptiveQualificationAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One adaptive qualification assignment, with what the planners need to act on it. */
data class AdaptiveQualificationAssignment(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var locale: String,
    var expiresAt: Int,
    var status: AdaptiveQualificationAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveQualificationAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveQualificationAssignmentStatus): Boolean {
        when (status) {
            AdaptiveQualificationAssignmentStatus.IDLE -> {
                when (next) {
                    AdaptiveQualificationAssignmentStatus.WARMING, AdaptiveQualificationAssignmentStatus.READY -> return true
                }
                return false
            }
            AdaptiveQualificationAssignmentStatus.WARMING -> return next == AdaptiveQualificationAssignmentStatus.READY
            AdaptiveQualificationAssignmentStatus.READY -> return next == AdaptiveQualificationAssignmentStatus.DRAINING || next == AdaptiveQualificationAssignmentStatus.STOPPED
            AdaptiveQualificationAssignmentStatus.DRAINING -> {
                when (next) {
                    AdaptiveQualificationAssignmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveQualificationAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("adaptive qualification assignment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive qualification assignment"} $code"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): AdaptiveQualificationAssignment {
        return AdaptiveQualificationAssignment(
            code = code,
            title = title,
            pinned = pinned,
            locale = locale,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive qualification assignment in status idle. */
fun newAdaptiveQualificationAssignment(code: String, title: String): AdaptiveQualificationAssignment {
    return AdaptiveQualificationAssignment(
        code = code,
        title = title,
        pinned = false,
        locale = "",
        expiresAt = 0,
        status = AdaptiveQualificationAssignmentStatus.IDLE,
    )
}
