package com.acme.transport.lastmile.attempt.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The attempt profile records of lastmile's attempt work, and the rules, stores, and calculations around
// them.

/** Where a attempt profile is in its life cycle. */
enum class AttemptProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A attempt profile as the attempt team keeps it. */
data class AttemptProfile(
    var code: String,
    var name: String,
    var category: String,
    var odometer: Int,
    var tags: MutableList<String>,
    var durationMinutes: Int,
    var ratio: Double,
    var contactEmail: String,
    var floorValue: Double,
    var stopCount: Int,
    var status: AttemptProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptProfileStatus): Boolean {
        when (status) {
            AttemptProfileStatus.OPEN -> {
                when (next) {
                    AttemptProfileStatus.IN_REVIEW, AttemptProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            AttemptProfileStatus.IN_REVIEW -> return next == AttemptProfileStatus.REOPENED
            AttemptProfileStatus.RESOLVED -> return next == AttemptProfileStatus.REOPENED || next == AttemptProfileStatus.ARCHIVED
            AttemptProfileStatus.REOPENED -> return next == AttemptProfileStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AttemptProfileStatus) {
        if (!canMoveTo(next)) {
            error("attempt profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"attempt profile"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedValue(): Double = round((stopCount.toDouble() - floorValue) * 100) / 100

    fun snapshot(): AttemptProfile {
        return AttemptProfile(
            code = code,
            name = name,
            category = category,
            odometer = odometer,
            tags = tags,
            durationMinutes = durationMinutes,
            ratio = ratio,
            contactEmail = contactEmail,
            floorValue = floorValue,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a attempt profile in status open. */
fun newAttemptProfile(code: String, name: String): AttemptProfile {
    return AttemptProfile(
        code = code,
        name = name,
        category = "",
        odometer = 0,
        tags = mutableListOf<String>(),
        durationMinutes = 0,
        ratio = 0.0,
        contactEmail = "",
        floorValue = 0.0,
        stopCount = 0,
        status = AttemptProfileStatus.OPEN,
    )
}
