package com.acme.transport.terminal.dwell.slot

import com.acme.transport.kit.text.normalize

/** Where a fallback dwell rule is in its life cycle. */
enum class FallbackDwellRuleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One fallback dwell rule, with what the planners need to act on it. */
data class FallbackDwellRule(
    var code: String,
    var title: String,
    var region: String,
    var etaMinutes: Int,
    var summary: String,
    var pinned: Boolean,
    var status: FallbackDwellRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDwellRuleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDwellRuleStatus): Boolean {
        when (status) {
            FallbackDwellRuleStatus.NEW -> return next == FallbackDwellRuleStatus.DONE || next == FallbackDwellRuleStatus.FAILED
            FallbackDwellRuleStatus.QUEUED -> {
                when (next) {
                    FallbackDwellRuleStatus.DONE -> return true
                }
                return false
            }
            FallbackDwellRuleStatus.RUNNING -> return next == FallbackDwellRuleStatus.FAILED
            FallbackDwellRuleStatus.DONE -> return next == FallbackDwellRuleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackDwellRuleStatus) {
        if (!canMoveTo(next)) {
            error("fallback dwell rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): FallbackDwellRule {
        return FallbackDwellRule(
            code = code,
            title = title,
            region = region,
            etaMinutes = etaMinutes,
            summary = summary,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback dwell rule in status new. */
fun newFallbackDwellRule(code: String, title: String): FallbackDwellRule {
    return FallbackDwellRule(
        code = code,
        title = title,
        region = "",
        etaMinutes = 0,
        summary = "",
        pinned = false,
        status = FallbackDwellRuleStatus.NEW,
    )
}
