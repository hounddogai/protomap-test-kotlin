package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.text.normalize

// The primary roster rule records of crewing's roster work, and the rules, stores, and calculations around
// them.

/** Where a primary roster rule is in its life cycle. */
enum class PrimaryRosterRuleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a primary roster rule and its current status. */
data class PrimaryRosterRule(
    var code: String,
    var name: String,
    var category: String,
    var timezone: String,
    var tags: MutableList<String>,
    var ownerId: String,
    var floorValue: Double,
    var status: PrimaryRosterRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRosterRuleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRosterRuleStatus): Boolean {
        when (status) {
            PrimaryRosterRuleStatus.NEW -> return next == PrimaryRosterRuleStatus.QUEUED || next == PrimaryRosterRuleStatus.FAILED
            PrimaryRosterRuleStatus.QUEUED -> return next == PrimaryRosterRuleStatus.RUNNING || next == PrimaryRosterRuleStatus.DONE
            PrimaryRosterRuleStatus.RUNNING -> return next == PrimaryRosterRuleStatus.DONE || next == PrimaryRosterRuleStatus.FAILED
            PrimaryRosterRuleStatus.DONE -> {
                when (next) {
                    PrimaryRosterRuleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryRosterRuleStatus) {
        if (!canMoveTo(next)) {
            error("primary roster rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = floorValue

    fun snapshot(): PrimaryRosterRule {
        return PrimaryRosterRule(
            code = code,
            name = name,
            category = category,
            timezone = timezone,
            tags = tags,
            ownerId = ownerId,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a primary roster rule in status new. */
fun newPrimaryRosterRule(code: String, name: String): PrimaryRosterRule {
    return PrimaryRosterRule(
        code = code,
        name = name,
        category = "",
        timezone = "",
        tags = mutableListOf<String>(),
        ownerId = "",
        floorValue = 0.0,
        status = PrimaryRosterRuleStatus.NEW,
    )
}
