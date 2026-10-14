package com.acme.transport.crewing.roster.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The roster rule records of crewing's roster work, and the rules, stores, and calculations around them.

/** Where a roster rule is in its life cycle. */
enum class RosterRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a roster rule and its current status. */
data class RosterRule(
    var code: String,
    var title: String,
    var limit: Int,
    var routeCode: String,
    var externalRef: String,
    var active: Boolean,
    var budget: Double,
    var floorValue: Double,
    var status: RosterRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterRuleStatus): Boolean {
        when (status) {
            RosterRuleStatus.REQUESTED -> {
                when (next) {
                    RosterRuleStatus.GRANTED, RosterRuleStatus.REVOKED -> return true
                }
                return false
            }
            RosterRuleStatus.GRANTED -> return next == RosterRuleStatus.REVOKED
            RosterRuleStatus.REVOKED -> return next == RosterRuleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RosterRuleStatus) {
        if (!canMoveTo(next)) {
            error("roster rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netTotal(): Double = round((floorValue + budget) * 100) / 100

    fun snapshot(): RosterRule {
        return RosterRule(
            code = code,
            title = title,
            limit = limit,
            routeCode = routeCode,
            externalRef = externalRef,
            active = active,
            budget = budget,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a roster rule in status requested. */
fun newRosterRule(code: String, title: String): RosterRule {
    return RosterRule(
        code = code,
        title = title,
        limit = 0,
        routeCode = "",
        externalRef = "",
        active = false,
        budget = 0.0,
        floorValue = 0.0,
        status = RosterRuleStatus.REQUESTED,
    )
}
