package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break rule is in its life cycle. */
enum class RestBreakRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rest break rule as the rest break team keeps it. */
data class RestBreakRule(
    var code: String,
    var name: String,
    var threshold: Double,
    var cost: Double,
    var createdAt: Int,
    var status: RestBreakRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakRuleStatus): Boolean {
        when (status) {
            RestBreakRuleStatus.IDLE -> return next == RestBreakRuleStatus.WARMING || next == RestBreakRuleStatus.STOPPED
            RestBreakRuleStatus.WARMING -> {
                when (next) {
                    RestBreakRuleStatus.READY -> return true
                }
                return false
            }
            RestBreakRuleStatus.READY -> {
                when (next) {
                    RestBreakRuleStatus.STOPPED -> return true
                }
                return false
            }
            RestBreakRuleStatus.DRAINING -> {
                when (next) {
                    RestBreakRuleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakRuleStatus) {
        if (!canMoveTo(next)) {
            error("rest break rule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rest break rule"} $code"

    fun adjustedExposure(): Double = round((threshold - createdAt.toDouble()) * 100) / 100

    fun snapshot(): RestBreakRule {
        return RestBreakRule(
            code = code,
            name = name,
            threshold = threshold,
            cost = cost,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rest break rule in status idle. */
fun newRestBreakRule(code: String, name: String): RestBreakRule {
    return RestBreakRule(
        code = code,
        name = name,
        threshold = 0.0,
        cost = 0.0,
        createdAt = 0,
        status = RestBreakRuleStatus.IDLE,
    )
}
