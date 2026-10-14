package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard rule is in its life cycle. */
enum class TrailerYardRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One trailer yard rule, with what the planners need to act on it. */
data class TrailerYardRule(
    var code: String,
    var label: String,
    var source: String,
    var cost: Double,
    var margin: Double,
    var etaMinutes: Int,
    var attempts: Int,
    var batchSize: Int,
    var budget: Double,
    var revision: Int,
    var status: TrailerYardRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardRuleStatus): Boolean {
        when (status) {
            TrailerYardRuleStatus.IDLE -> return next == TrailerYardRuleStatus.WARMING || next == TrailerYardRuleStatus.STOPPED
            TrailerYardRuleStatus.WARMING -> return next == TrailerYardRuleStatus.STOPPED
            TrailerYardRuleStatus.READY -> return next == TrailerYardRuleStatus.DRAINING || next == TrailerYardRuleStatus.STOPPED
            TrailerYardRuleStatus.DRAINING -> {
                when (next) {
                    TrailerYardRuleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardRuleStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard rule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"trailer yard rule"} $code"

    fun projectedTotal(): Double = round((margin - cost) * 100) / 100

    fun snapshot(): TrailerYardRule {
        return TrailerYardRule(
            code = code,
            label = label,
            source = source,
            cost = cost,
            margin = margin,
            etaMinutes = etaMinutes,
            attempts = attempts,
            batchSize = batchSize,
            budget = budget,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a trailer yard rule in status idle. */
fun newTrailerYardRule(code: String, label: String): TrailerYardRule {
    return TrailerYardRule(
        code = code,
        label = label,
        source = "",
        cost = 0.0,
        margin = 0.0,
        etaMinutes = 0,
        attempts = 0,
        batchSize = 0,
        budget = 0.0,
        revision = 0,
        status = TrailerYardRuleStatus.IDLE,
    )
}
