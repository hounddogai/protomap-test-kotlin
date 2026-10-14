package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy backhaul rule is in its life cycle. */
enum class LegacyBackhaulRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy backhaul rule. Its code is unique within a tenant. */
data class LegacyBackhaulRule(
    var code: String,
    var label: String,
    var title: String,
    var locale: String,
    var windowDays: Int,
    var startsAt: Int,
    var depot: String,
    var amount: Double,
    var status: LegacyBackhaulRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyBackhaulRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyBackhaulRuleStatus): Boolean {
        when (status) {
            LegacyBackhaulRuleStatus.PROPOSED -> return next == LegacyBackhaulRuleStatus.REJECTED
            LegacyBackhaulRuleStatus.APPROVED -> return next == LegacyBackhaulRuleStatus.REJECTED
            LegacyBackhaulRuleStatus.REJECTED -> return next == LegacyBackhaulRuleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyBackhaulRuleStatus) {
        if (!canMoveTo(next)) {
            error("legacy backhaul rule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveExposure(): Double = round(windowDays.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): LegacyBackhaulRule {
        return LegacyBackhaulRule(
            code = code,
            label = label,
            title = title,
            locale = locale,
            windowDays = windowDays,
            startsAt = startsAt,
            depot = depot,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a legacy backhaul rule in status proposed. */
fun newLegacyBackhaulRule(code: String, label: String): LegacyBackhaulRule {
    return LegacyBackhaulRule(
        code = code,
        label = label,
        title = "",
        locale = "",
        windowDays = 0,
        startsAt = 0,
        depot = "",
        amount = 0.0,
        status = LegacyBackhaulRuleStatus.PROPOSED,
    )
}
