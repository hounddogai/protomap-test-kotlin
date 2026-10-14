package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt override is in its life cycle. */
enum class AttemptOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A attempt override as the attempt team keeps it. */
data class AttemptOverride(
    var code: String,
    var title: String,
    var limit: Int,
    var pinned: Boolean,
    var description: String,
    var distanceKm: Double,
    var capacity: Int,
    var status: AttemptOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptOverrideStatus): Boolean {
        when (status) {
            AttemptOverrideStatus.OPEN -> return next == AttemptOverrideStatus.RESOLVED
            AttemptOverrideStatus.IN_REVIEW -> return next == AttemptOverrideStatus.ARCHIVED
            AttemptOverrideStatus.RESOLVED -> return next == AttemptOverrideStatus.REOPENED
            AttemptOverrideStatus.REOPENED -> {
                when (next) {
                    AttemptOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptOverrideStatus) {
        if (!canMoveTo(next)) {
            error("attempt override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedExposure(): Double = round((limit.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): AttemptOverride {
        return AttemptOverride(
            code = code,
            title = title,
            limit = limit,
            pinned = pinned,
            description = description,
            distanceKm = distanceKm,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a attempt override in status open. */
fun newAttemptOverride(code: String, title: String): AttemptOverride {
    return AttemptOverride(
        code = code,
        title = title,
        limit = 0,
        pinned = false,
        description = "",
        distanceKm = 0.0,
        capacity = 0,
        status = AttemptOverrideStatus.OPEN,
    )
}
