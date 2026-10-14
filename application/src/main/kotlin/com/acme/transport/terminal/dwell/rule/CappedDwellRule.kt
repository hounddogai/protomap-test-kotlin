package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped dwell rule records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a capped dwell rule is in its life cycle. */
enum class CappedDwellRuleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a capped dwell rule and its current status. */
data class CappedDwellRule(
    var code: String,
    var label: String,
    var windowDays: Int,
    var unit: String,
    var revision: Int,
    var status: CappedDwellRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDwellRuleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDwellRuleStatus): Boolean {
        when (status) {
            CappedDwellRuleStatus.OPEN -> return next == CappedDwellRuleStatus.IN_REVIEW || next == CappedDwellRuleStatus.ARCHIVED
            CappedDwellRuleStatus.IN_REVIEW -> return next == CappedDwellRuleStatus.REOPENED
            CappedDwellRuleStatus.RESOLVED -> return next == CappedDwellRuleStatus.ARCHIVED
            CappedDwellRuleStatus.REOPENED -> return next == CappedDwellRuleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CappedDwellRuleStatus) {
        if (!canMoveTo(next)) {
            error("capped dwell rule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"capped dwell rule"} $code"

    fun projectedTotal(): Double = round((revision.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): CappedDwellRule {
        return CappedDwellRule(
            code = code,
            label = label,
            windowDays = windowDays,
            unit = unit,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a capped dwell rule in status open. */
fun newCappedDwellRule(code: String, label: String): CappedDwellRule {
    return CappedDwellRule(
        code = code,
        label = label,
        windowDays = 0,
        unit = "",
        revision = 0,
        status = CappedDwellRuleStatus.OPEN,
    )
}
