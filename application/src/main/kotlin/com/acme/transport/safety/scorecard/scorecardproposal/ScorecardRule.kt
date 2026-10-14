package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard rule is in its life cycle. */
enum class ScorecardRuleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scorecard rule. Its code is unique within a tenant. */
data class ScorecardRule(
    var code: String,
    var title: String,
    var externalRef: String,
    var minimum: Double,
    var dock: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var routeCode: String,
    var durationMinutes: Int,
    var source: String,
    var version: Int,
    var status: ScorecardRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardRuleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardRuleStatus): Boolean {
        when (status) {
            ScorecardRuleStatus.OPEN -> {
                when (next) {
                    ScorecardRuleStatus.ARCHIVED -> return true
                }
                return false
            }
            ScorecardRuleStatus.IN_REVIEW -> return next == ScorecardRuleStatus.RESOLVED || next == ScorecardRuleStatus.ARCHIVED
            ScorecardRuleStatus.RESOLVED -> {
                when (next) {
                    ScorecardRuleStatus.REOPENED, ScorecardRuleStatus.ARCHIVED -> return true
                }
                return false
            }
            ScorecardRuleStatus.REOPENED -> return next == ScorecardRuleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScorecardRuleStatus) {
        if (!canMoveTo(next)) {
            error("scorecard rule $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"scorecard rule"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((minimum + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): ScorecardRule {
        return ScorecardRule(
            code = code,
            title = title,
            externalRef = externalRef,
            minimum = minimum,
            dock = dock,
            label = label,
            attributes = attributes,
            routeCode = routeCode,
            durationMinutes = durationMinutes,
            source = source,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a scorecard rule in status open. */
fun newScorecardRule(code: String, title: String): ScorecardRule {
    return ScorecardRule(
        code = code,
        title = title,
        externalRef = "",
        minimum = 0.0,
        dock = "",
        label = "",
        attributes = mutableMapOf<String, String>(),
        routeCode = "",
        durationMinutes = 0,
        source = "",
        version = 0,
        status = ScorecardRuleStatus.OPEN,
    )
}
