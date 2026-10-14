package com.acme.transport.linehaul.relay.score

import com.acme.transport.kit.text.normalize

// The rolling relay score records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a rolling relay score is in its life cycle. */
enum class RollingRelayScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling relay score. Its code is unique within a tenant. */
data class RollingRelayScore(
    var code: String,
    var name: String,
    var trailerId: String,
    var label: String,
    var summary: String,
    var amount: Double,
    var status: RollingRelayScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRelayScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRelayScoreStatus): Boolean {
        when (status) {
            RollingRelayScoreStatus.CREATED -> {
                when (next) {
                    RollingRelayScoreStatus.PUBLISHED -> return true
                }
                return false
            }
            RollingRelayScoreStatus.VALIDATED -> {
                when (next) {
                    RollingRelayScoreStatus.RETIRED -> return true
                }
                return false
            }
            RollingRelayScoreStatus.PUBLISHED -> return next == RollingRelayScoreStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingRelayScoreStatus) {
        if (!canMoveTo(next)) {
            error("rolling relay score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling relay score"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): RollingRelayScore {
        return RollingRelayScore(
            code = code,
            name = name,
            trailerId = trailerId,
            label = label,
            summary = summary,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling relay score in status created. */
fun newRollingRelayScore(code: String, name: String): RollingRelayScore {
    return RollingRelayScore(
        code = code,
        name = name,
        trailerId = "",
        label = "",
        summary = "",
        amount = 0.0,
        status = RollingRelayScoreStatus.CREATED,
    )
}
