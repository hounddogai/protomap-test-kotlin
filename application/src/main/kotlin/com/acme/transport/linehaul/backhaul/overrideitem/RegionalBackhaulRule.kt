package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional backhaul rule is in its life cycle. */
enum class RegionalBackhaulRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One regional backhaul rule, with what the planners need to act on it. */
data class RegionalBackhaulRule(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var trailerId: String,
    var description: String,
    var minimum: Double,
    var graceDays: Int,
    var tags: MutableList<String>,
    var status: RegionalBackhaulRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalBackhaulRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalBackhaulRuleStatus): Boolean {
        when (status) {
            RegionalBackhaulRuleStatus.DRAFT -> return next == RegionalBackhaulRuleStatus.ACTIVE
            RegionalBackhaulRuleStatus.PENDING -> return next == RegionalBackhaulRuleStatus.CLOSED
            RegionalBackhaulRuleStatus.ACTIVE -> {
                when (next) {
                    RegionalBackhaulRuleStatus.SUSPENDED, RegionalBackhaulRuleStatus.CLOSED -> return true
                }
                return false
            }
            RegionalBackhaulRuleStatus.SUSPENDED -> {
                when (next) {
                    RegionalBackhaulRuleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalBackhaulRuleStatus) {
        if (!canMoveTo(next)) {
            error("regional backhaul rule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round((graceDays.toDouble() + minimum) * 100) / 100

    fun snapshot(): RegionalBackhaulRule {
        return RegionalBackhaulRule(
            code = code,
            label = label,
            expiresAt = expiresAt,
            trailerId = trailerId,
            description = description,
            minimum = minimum,
            graceDays = graceDays,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional backhaul rule in status draft. */
fun newRegionalBackhaulRule(code: String, label: String): RegionalBackhaulRule {
    return RegionalBackhaulRule(
        code = code,
        label = label,
        expiresAt = 0,
        trailerId = "",
        description = "",
        minimum = 0.0,
        graceDays = 0,
        tags = mutableListOf<String>(),
        status = RegionalBackhaulRuleStatus.DRAFT,
    )
}
