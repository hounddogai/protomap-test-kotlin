package com.acme.transport.safety.scorecard.scorecardwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending scorecard policy is in its life cycle. */
enum class PendingScorecardPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A pending scorecard policy as the scorecard team keeps it. */
data class PendingScorecardPolicy(
    var code: String,
    var title: String,
    var source: String,
    var sequence: Int,
    var pinned: Boolean,
    var durationMinutes: Int,
    var limit: Int,
    var tags: MutableList<String>,
    var channel: String,
    var unit: String,
    var segment: String,
    var status: PendingScorecardPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingScorecardPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingScorecardPolicyStatus): Boolean {
        when (status) {
            PendingScorecardPolicyStatus.PROPOSED -> {
                when (next) {
                    PendingScorecardPolicyStatus.APPROVED, PendingScorecardPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingScorecardPolicyStatus.APPROVED -> return next == PendingScorecardPolicyStatus.REJECTED
            PendingScorecardPolicyStatus.REJECTED -> return next == PendingScorecardPolicyStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PendingScorecardPolicyStatus) {
        if (!canMoveTo(next)) {
            error("pending scorecard policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedTotal(): Double = round((limit.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): PendingScorecardPolicy {
        return PendingScorecardPolicy(
            code = code,
            title = title,
            source = source,
            sequence = sequence,
            pinned = pinned,
            durationMinutes = durationMinutes,
            limit = limit,
            tags = tags,
            channel = channel,
            unit = unit,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a pending scorecard policy in status proposed. */
fun newPendingScorecardPolicy(code: String, title: String): PendingScorecardPolicy {
    return PendingScorecardPolicy(
        code = code,
        title = title,
        source = "",
        sequence = 0,
        pinned = false,
        durationMinutes = 0,
        limit = 0,
        tags = mutableListOf<String>(),
        channel = "",
        unit = "",
        segment = "",
        status = PendingScorecardPolicyStatus.PROPOSED,
    )
}
