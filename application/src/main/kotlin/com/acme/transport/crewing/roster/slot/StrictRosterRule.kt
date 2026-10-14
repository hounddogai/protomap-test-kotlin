package com.acme.transport.crewing.roster.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict roster rule is in its life cycle. */
enum class StrictRosterRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a strict roster rule and its current status. */
data class StrictRosterRule(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var region: String,
    var pinned: Boolean,
    var expiresAt: Int,
    var version: Int,
    var distanceKm: Double,
    var etaMinutes: Int,
    var windowDays: Int,
    var depot: String,
    var status: StrictRosterRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRosterRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRosterRuleStatus): Boolean {
        when (status) {
            StrictRosterRuleStatus.PROPOSED -> return next == StrictRosterRuleStatus.REJECTED
            StrictRosterRuleStatus.APPROVED -> return next == StrictRosterRuleStatus.WITHDRAWN
            StrictRosterRuleStatus.REJECTED -> {
                when (next) {
                    StrictRosterRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictRosterRuleStatus) {
        if (!canMoveTo(next)) {
            error("strict roster rule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
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

    fun adjustedTotal(): Double = round((expiresAt.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): StrictRosterRule {
        return StrictRosterRule(
            code = code,
            title = title,
            tags = tags,
            region = region,
            pinned = pinned,
            expiresAt = expiresAt,
            version = version,
            distanceKm = distanceKm,
            etaMinutes = etaMinutes,
            windowDays = windowDays,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict roster rule in status proposed. */
fun newStrictRosterRule(code: String, title: String): StrictRosterRule {
    return StrictRosterRule(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        region = "",
        pinned = false,
        expiresAt = 0,
        version = 0,
        distanceKm = 0.0,
        etaMinutes = 0,
        windowDays = 0,
        depot = "",
        status = StrictRosterRuleStatus.PROPOSED,
    )
}
