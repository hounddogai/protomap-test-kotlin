package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional fuel burn rule records of emissions's fuel burn work, and the rules, stores, and calculations
// around them.

/** Where a regional fuel burn rule is in its life cycle. */
enum class RegionalFuelBurnRuleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A regional fuel burn rule. Its code is unique within a tenant. */
data class RegionalFuelBurnRule(
    var code: String,
    var title: String,
    var amount: Double,
    var score: Double,
    var active: Boolean,
    var tags: MutableList<String>,
    var channel: String,
    var ratio: Double,
    var minimum: Double,
    var windowDays: Int,
    var status: RegionalFuelBurnRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFuelBurnRuleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFuelBurnRuleStatus): Boolean {
        when (status) {
            RegionalFuelBurnRuleStatus.OPEN -> {
                when (next) {
                    RegionalFuelBurnRuleStatus.REOPENED -> return true
                }
                return false
            }
            RegionalFuelBurnRuleStatus.IN_REVIEW -> {
                when (next) {
                    RegionalFuelBurnRuleStatus.REOPENED, RegionalFuelBurnRuleStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalFuelBurnRuleStatus.RESOLVED -> return next == RegionalFuelBurnRuleStatus.REOPENED || next == RegionalFuelBurnRuleStatus.ARCHIVED
            RegionalFuelBurnRuleStatus.REOPENED -> {
                when (next) {
                    RegionalFuelBurnRuleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalFuelBurnRuleStatus) {
        if (!canMoveTo(next)) {
            error("regional fuel burn rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional fuel burn rule"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round(amount * minimum * 100) / 100

    fun snapshot(): RegionalFuelBurnRule {
        return RegionalFuelBurnRule(
            code = code,
            title = title,
            amount = amount,
            score = score,
            active = active,
            tags = tags,
            channel = channel,
            ratio = ratio,
            minimum = minimum,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional fuel burn rule in status open. */
fun newRegionalFuelBurnRule(code: String, title: String): RegionalFuelBurnRule {
    return RegionalFuelBurnRule(
        code = code,
        title = title,
        amount = 0.0,
        score = 0.0,
        active = false,
        tags = mutableListOf<String>(),
        channel = "",
        ratio = 0.0,
        minimum = 0.0,
        windowDays = 0,
        status = RegionalFuelBurnRuleStatus.OPEN,
    )
}
