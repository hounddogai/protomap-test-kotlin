package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.text.normalize

/** Where a attempt rule is in its life cycle. */
enum class AttemptRuleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A attempt rule as the attempt team keeps it. */
data class AttemptRule(
    var code: String,
    var label: String,
    var discount: Double,
    var name: String,
    var currency: String,
    var status: AttemptRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptRuleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptRuleStatus): Boolean {
        when (status) {
            AttemptRuleStatus.NEW -> return next == AttemptRuleStatus.DONE || next == AttemptRuleStatus.FAILED
            AttemptRuleStatus.QUEUED -> return next == AttemptRuleStatus.FAILED
            AttemptRuleStatus.RUNNING -> return next == AttemptRuleStatus.DONE || next == AttemptRuleStatus.FAILED
            AttemptRuleStatus.DONE -> {
                when (next) {
                    AttemptRuleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptRuleStatus) {
        if (!canMoveTo(next)) {
            error("attempt rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"attempt rule"} $code"

    fun measuredValue(): Double = discount

    fun snapshot(): AttemptRule {
        return AttemptRule(
            code = code,
            label = label,
            discount = discount,
            name = name,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a attempt rule in status new. */
fun newAttemptRule(code: String, label: String): AttemptRule {
    return AttemptRule(
        code = code,
        label = label,
        discount = 0.0,
        name = "",
        currency = "",
        status = AttemptRuleStatus.NEW,
    )
}
