package com.acme.transport.emissions.idling.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling rule is in its life cycle. */
enum class IdlingRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a idling rule and its current status. */
data class IdlingRule(
    var code: String,
    var title: String,
    var stopCount: Int,
    var depot: String,
    var threshold: Double,
    var quantity: Int,
    var status: IdlingRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingRuleStatus): Boolean {
        when (status) {
            IdlingRuleStatus.IDLE -> return next == IdlingRuleStatus.READY
            IdlingRuleStatus.WARMING -> {
                when (next) {
                    IdlingRuleStatus.DRAINING -> return true
                }
                return false
            }
            IdlingRuleStatus.READY -> return next == IdlingRuleStatus.DRAINING || next == IdlingRuleStatus.STOPPED
            IdlingRuleStatus.DRAINING -> {
                when (next) {
                    IdlingRuleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingRuleStatus) {
        if (!canMoveTo(next)) {
            error("idling rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"idling rule"} $code"

    fun adjustedLoad(): Double = round((stopCount.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): IdlingRule {
        return IdlingRule(
            code = code,
            title = title,
            stopCount = stopCount,
            depot = depot,
            threshold = threshold,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a idling rule in status idle. */
fun newIdlingRule(code: String, title: String): IdlingRule {
    return IdlingRule(
        code = code,
        title = title,
        stopCount = 0,
        depot = "",
        threshold = 0.0,
        quantity = 0,
        status = IdlingRuleStatus.IDLE,
    )
}
