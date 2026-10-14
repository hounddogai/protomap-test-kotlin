package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.kit.text.normalize

// The strict route offset rule records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a strict route offset rule is in its life cycle. */
enum class StrictRouteOffsetRuleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One strict route offset rule, with what the planners need to act on it. */
data class StrictRouteOffsetRule(
    var code: String,
    var label: String,
    var checksum: String,
    var margin: Double,
    var locale: String,
    var status: StrictRouteOffsetRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRouteOffsetRuleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRouteOffsetRuleStatus): Boolean {
        when (status) {
            StrictRouteOffsetRuleStatus.NEW -> {
                when (next) {
                    StrictRouteOffsetRuleStatus.RUNNING, StrictRouteOffsetRuleStatus.DONE -> return true
                }
                return false
            }
            StrictRouteOffsetRuleStatus.QUEUED -> return next == StrictRouteOffsetRuleStatus.RUNNING || next == StrictRouteOffsetRuleStatus.FAILED
            StrictRouteOffsetRuleStatus.RUNNING -> return next == StrictRouteOffsetRuleStatus.FAILED
            StrictRouteOffsetRuleStatus.DONE -> return next == StrictRouteOffsetRuleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictRouteOffsetRuleStatus) {
        if (!canMoveTo(next)) {
            error("strict route offset rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = margin

    fun snapshot(): StrictRouteOffsetRule {
        return StrictRouteOffsetRule(
            code = code,
            label = label,
            checksum = checksum,
            margin = margin,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a strict route offset rule in status new. */
fun newStrictRouteOffsetRule(code: String, label: String): StrictRouteOffsetRule {
    return StrictRouteOffsetRule(
        code = code,
        label = label,
        checksum = "",
        margin = 0.0,
        locale = "",
        status = StrictRouteOffsetRuleStatus.NEW,
    )
}
