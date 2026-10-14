package com.acme.transport.linehaul.laneplan.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback lane plan rule is in its life cycle. */
enum class FallbackLanePlanRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fallback lane plan rule, with what the planners need to act on it. */
data class FallbackLanePlanRule(
    var code: String,
    var label: String,
    var createdAt: Int,
    var summary: String,
    var reviewer: String,
    var threshold: Double,
    var updatedAt: Int,
    var status: FallbackLanePlanRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackLanePlanRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackLanePlanRuleStatus): Boolean {
        when (status) {
            FallbackLanePlanRuleStatus.DRAFT -> return next == FallbackLanePlanRuleStatus.ACTIVE || next == FallbackLanePlanRuleStatus.CLOSED
            FallbackLanePlanRuleStatus.PENDING -> return next == FallbackLanePlanRuleStatus.ACTIVE || next == FallbackLanePlanRuleStatus.SUSPENDED
            FallbackLanePlanRuleStatus.ACTIVE -> {
                when (next) {
                    FallbackLanePlanRuleStatus.CLOSED -> return true
                }
                return false
            }
            FallbackLanePlanRuleStatus.SUSPENDED -> {
                when (next) {
                    FallbackLanePlanRuleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackLanePlanRuleStatus) {
        if (!canMoveTo(next)) {
            error("fallback lane plan rule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedExposure(): Double = round(createdAt.toDouble() * threshold * 100) / 100

    fun snapshot(): FallbackLanePlanRule {
        return FallbackLanePlanRule(
            code = code,
            label = label,
            createdAt = createdAt,
            summary = summary,
            reviewer = reviewer,
            threshold = threshold,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback lane plan rule in status draft. */
fun newFallbackLanePlanRule(code: String, label: String): FallbackLanePlanRule {
    return FallbackLanePlanRule(
        code = code,
        label = label,
        createdAt = 0,
        summary = "",
        reviewer = "",
        threshold = 0.0,
        updatedAt = 0,
        status = FallbackLanePlanRuleStatus.DRAFT,
    )
}
