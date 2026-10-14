package com.acme.transport.safety.coaching.profile

import com.acme.transport.kit.text.normalize

// The coaching profile records of safety's coaching work, and the rules, stores, and calculations around
// them.

/** Where a coaching profile is in its life cycle. */
enum class CoachingProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a coaching profile and its current status. */
data class CoachingProfile(
    var code: String,
    var title: String,
    var summary: String,
    var weight: Double,
    var externalRef: String,
    var status: CoachingProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingProfileStatus): Boolean {
        when (status) {
            CoachingProfileStatus.NEW -> return next == CoachingProfileStatus.RUNNING
            CoachingProfileStatus.QUEUED -> return next == CoachingProfileStatus.RUNNING || next == CoachingProfileStatus.DONE
            CoachingProfileStatus.RUNNING -> {
                when (next) {
                    CoachingProfileStatus.DONE, CoachingProfileStatus.FAILED -> return true
                }
                return false
            }
            CoachingProfileStatus.DONE -> {
                when (next) {
                    CoachingProfileStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingProfileStatus) {
        if (!canMoveTo(next)) {
            error("coaching profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"coaching profile"} $code"

    fun measuredValue(): Double = weight

    fun snapshot(): CoachingProfile {
        return CoachingProfile(
            code = code,
            title = title,
            summary = summary,
            weight = weight,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a coaching profile in status new. */
fun newCoachingProfile(code: String, title: String): CoachingProfile {
    return CoachingProfile(
        code = code,
        title = title,
        summary = "",
        weight = 0.0,
        externalRef = "",
        status = CoachingProfileStatus.NEW,
    )
}
