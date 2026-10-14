package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.text.normalize

/** Where a scorecard group is in its life cycle. */
enum class ScorecardGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scorecard group. Its code is unique within a tenant. */
data class ScorecardGroup(
    var code: String,
    var name: String,
    var ownerId: String,
    var pinned: Boolean,
    var phone: String,
    var score: Double,
    var status: ScorecardGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardGroupStatus): Boolean {
        when (status) {
            ScorecardGroupStatus.DRAFT -> return next == ScorecardGroupStatus.ACTIVE || next == ScorecardGroupStatus.SUSPENDED
            ScorecardGroupStatus.PENDING -> {
                when (next) {
                    ScorecardGroupStatus.SUSPENDED, ScorecardGroupStatus.CLOSED -> return true
                }
                return false
            }
            ScorecardGroupStatus.ACTIVE -> {
                when (next) {
                    ScorecardGroupStatus.SUSPENDED, ScorecardGroupStatus.CLOSED -> return true
                }
                return false
            }
            ScorecardGroupStatus.SUSPENDED -> return next == ScorecardGroupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScorecardGroupStatus) {
        if (!canMoveTo(next)) {
            error("scorecard group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scorecard group"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): ScorecardGroup {
        return ScorecardGroup(
            code = code,
            name = name,
            ownerId = ownerId,
            pinned = pinned,
            phone = phone,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scorecard group in status draft. */
fun newScorecardGroup(code: String, name: String): ScorecardGroup {
    return ScorecardGroup(
        code = code,
        name = name,
        ownerId = "",
        pinned = false,
        phone = "",
        score = 0.0,
        status = ScorecardGroupStatus.DRAFT,
    )
}
