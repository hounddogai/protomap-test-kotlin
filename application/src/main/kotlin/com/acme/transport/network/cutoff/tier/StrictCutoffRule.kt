package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict cutoff rule is in its life cycle. */
enum class StrictCutoffRuleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict cutoff rule. Its code is unique within a tenant. */
data class StrictCutoffRule(
    var code: String,
    var label: String,
    var ratio: Double,
    var amount: Double,
    var windowDays: Int,
    var status: StrictCutoffRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCutoffRuleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCutoffRuleStatus): Boolean {
        when (status) {
            StrictCutoffRuleStatus.NEW -> {
                when (next) {
                    StrictCutoffRuleStatus.RUNNING, StrictCutoffRuleStatus.DONE -> return true
                }
                return false
            }
            StrictCutoffRuleStatus.QUEUED -> return next == StrictCutoffRuleStatus.RUNNING
            StrictCutoffRuleStatus.RUNNING -> {
                when (next) {
                    StrictCutoffRuleStatus.DONE -> return true
                }
                return false
            }
            StrictCutoffRuleStatus.DONE -> {
                when (next) {
                    StrictCutoffRuleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictCutoffRuleStatus) {
        if (!canMoveTo(next)) {
            error("strict cutoff rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedExposure(): Double = round((ratio - amount) * 100) / 100

    fun snapshot(): StrictCutoffRule {
        return StrictCutoffRule(
            code = code,
            label = label,
            ratio = ratio,
            amount = amount,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict cutoff rule in status new. */
fun newStrictCutoffRule(code: String, label: String): StrictCutoffRule {
    return StrictCutoffRule(
        code = code,
        label = label,
        ratio = 0.0,
        amount = 0.0,
        windowDays = 0,
        status = StrictCutoffRuleStatus.NEW,
    )
}
