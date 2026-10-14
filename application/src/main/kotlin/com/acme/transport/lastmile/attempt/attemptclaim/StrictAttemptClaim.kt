package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.text.normalize

// The strict attempt claim records of lastmile's attempt work, and the rules, stores, and calculations around
// them.

/** Where a strict attempt claim is in its life cycle. */
enum class StrictAttemptClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a strict attempt claim and its current status. */
data class StrictAttemptClaim(
    var code: String,
    var title: String,
    var notes: String,
    var visible: Boolean,
    var dock: String,
    var lane: String,
    var startsAt: Int,
    var depot: String,
    var tags: MutableList<String>,
    var status: StrictAttemptClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAttemptClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAttemptClaimStatus): Boolean {
        when (status) {
            StrictAttemptClaimStatus.PLANNED -> {
                when (next) {
                    StrictAttemptClaimStatus.COMPLETED, StrictAttemptClaimStatus.CANCELLED -> return true
                }
                return false
            }
            StrictAttemptClaimStatus.SCHEDULED -> {
                when (next) {
                    StrictAttemptClaimStatus.IN_PROGRESS, StrictAttemptClaimStatus.COMPLETED -> return true
                }
                return false
            }
            StrictAttemptClaimStatus.IN_PROGRESS -> return next == StrictAttemptClaimStatus.COMPLETED
            StrictAttemptClaimStatus.COMPLETED -> {
                when (next) {
                    StrictAttemptClaimStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictAttemptClaimStatus) {
        if (!canMoveTo(next)) {
            error("strict attempt claim $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
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

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): StrictAttemptClaim {
        return StrictAttemptClaim(
            code = code,
            title = title,
            notes = notes,
            visible = visible,
            dock = dock,
            lane = lane,
            startsAt = startsAt,
            depot = depot,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict attempt claim in status planned. */
fun newStrictAttemptClaim(code: String, title: String): StrictAttemptClaim {
    return StrictAttemptClaim(
        code = code,
        title = title,
        notes = "",
        visible = false,
        dock = "",
        lane = "",
        startsAt = 0,
        depot = "",
        tags = mutableListOf<String>(),
        status = StrictAttemptClaimStatus.PLANNED,
    )
}
