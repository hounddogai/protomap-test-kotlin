package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered coaching audit is in its life cycle. */
enum class TieredCoachingAuditStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One tiered coaching audit, with what the planners need to act on it. */
data class TieredCoachingAudit(
    var code: String,
    var name: String,
    var startsAt: Int,
    var endsAt: Int,
    var active: Boolean,
    var reviewer: String,
    var amount: Double,
    var status: TieredCoachingAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCoachingAuditStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCoachingAuditStatus): Boolean {
        when (status) {
            TieredCoachingAuditStatus.CREATED -> {
                when (next) {
                    TieredCoachingAuditStatus.RETIRED -> return true
                }
                return false
            }
            TieredCoachingAuditStatus.VALIDATED -> {
                when (next) {
                    TieredCoachingAuditStatus.PUBLISHED, TieredCoachingAuditStatus.RETIRED -> return true
                }
                return false
            }
            TieredCoachingAuditStatus.PUBLISHED -> return next == TieredCoachingAuditStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredCoachingAuditStatus) {
        if (!canMoveTo(next)) {
            error("tiered coaching audit $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedVolume(): Double = round(endsAt.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): TieredCoachingAudit {
        return TieredCoachingAudit(
            code = code,
            name = name,
            startsAt = startsAt,
            endsAt = endsAt,
            active = active,
            reviewer = reviewer,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered coaching audit in status created. */
fun newTieredCoachingAudit(code: String, name: String): TieredCoachingAudit {
    return TieredCoachingAudit(
        code = code,
        name = name,
        startsAt = 0,
        endsAt = 0,
        active = false,
        reviewer = "",
        amount = 0.0,
        status = TieredCoachingAuditStatus.CREATED,
    )
}
