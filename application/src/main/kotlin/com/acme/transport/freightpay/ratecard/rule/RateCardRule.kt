package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.kit.text.normalize

// The rate card rule records of freightpay's rate card work, and the rules, stores, and calculations around
// them.

/** Where a rate card rule is in its life cycle. */
enum class RateCardRuleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One rate card rule, with what the planners need to act on it. */
data class RateCardRule(
    var code: String,
    var label: String,
    var depot: String,
    var cost: Double,
    var dock: String,
    var status: RateCardRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardRuleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardRuleStatus): Boolean {
        when (status) {
            RateCardRuleStatus.NEW -> return next == RateCardRuleStatus.QUEUED || next == RateCardRuleStatus.RUNNING
            RateCardRuleStatus.QUEUED -> {
                when (next) {
                    RateCardRuleStatus.RUNNING -> return true
                }
                return false
            }
            RateCardRuleStatus.RUNNING -> return next == RateCardRuleStatus.DONE
            RateCardRuleStatus.DONE -> return next == RateCardRuleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RateCardRuleStatus) {
        if (!canMoveTo(next)) {
            error("rate card rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = cost

    fun snapshot(): RateCardRule {
        return RateCardRule(code = code, label = label, depot = depot, cost = cost, dock = dock, status = status)
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rate card rule in status new. */
fun newRateCardRule(code: String, label: String): RateCardRule {
    return RateCardRule(
        code = code,
        label = label,
        depot = "",
        cost = 0.0,
        dock = "",
        status = RateCardRuleStatus.NEW,
    )
}
