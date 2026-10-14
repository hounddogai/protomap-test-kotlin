package com.acme.transport.network.cutoff.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff rule is in its life cycle. */
enum class CutoffRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a cutoff rule and its current status. */
data class CutoffRule(
    var code: String,
    var title: String,
    var priority: Int,
    var summary: String,
    var etaMinutes: Int,
    var status: CutoffRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffRuleStatus): Boolean {
        when (status) {
            CutoffRuleStatus.PROPOSED -> return next == CutoffRuleStatus.APPROVED
            CutoffRuleStatus.APPROVED -> {
                when (next) {
                    CutoffRuleStatus.REJECTED -> return true
                }
                return false
            }
            CutoffRuleStatus.REJECTED -> {
                when (next) {
                    CutoffRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffRuleStatus) {
        if (!canMoveTo(next)) {
            error("cutoff rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"cutoff rule"} $code"

    fun adjustedVolume(): Double = round(priority.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): CutoffRule {
        return CutoffRule(
            code = code,
            title = title,
            priority = priority,
            summary = summary,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cutoff rule in status proposed. */
fun newCutoffRule(code: String, title: String): CutoffRule {
    return CutoffRule(
        code = code,
        title = title,
        priority = 0,
        summary = "",
        etaMinutes = 0,
        status = CutoffRuleStatus.PROPOSED,
    )
}
