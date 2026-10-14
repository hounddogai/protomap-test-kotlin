package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling trailer yard rule is in its life cycle. */
enum class RollingTrailerYardRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling trailer yard rule. Its code is unique within a tenant. */
data class RollingTrailerYardRule(
    var code: String,
    var title: String,
    var visible: Boolean,
    var stopCount: Int,
    var cost: Double,
    var status: RollingTrailerYardRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTrailerYardRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTrailerYardRuleStatus): Boolean {
        when (status) {
            RollingTrailerYardRuleStatus.IDLE -> {
                when (next) {
                    RollingTrailerYardRuleStatus.WARMING, RollingTrailerYardRuleStatus.DRAINING -> return true
                }
                return false
            }
            RollingTrailerYardRuleStatus.WARMING -> return next == RollingTrailerYardRuleStatus.READY || next == RollingTrailerYardRuleStatus.DRAINING
            RollingTrailerYardRuleStatus.READY -> {
                when (next) {
                    RollingTrailerYardRuleStatus.STOPPED -> return true
                }
                return false
            }
            RollingTrailerYardRuleStatus.DRAINING -> {
                when (next) {
                    RollingTrailerYardRuleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTrailerYardRuleStatus) {
        if (!canMoveTo(next)) {
            error("rolling trailer yard rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling trailer yard rule"} $code"

    fun adjustedLoad(): Double = round(stopCount.toDouble() * cost * 100) / 100

    fun snapshot(): RollingTrailerYardRule {
        return RollingTrailerYardRule(
            code = code,
            title = title,
            visible = visible,
            stopCount = stopCount,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling trailer yard rule in status idle. */
fun newRollingTrailerYardRule(code: String, title: String): RollingTrailerYardRule {
    return RollingTrailerYardRule(
        code = code,
        title = title,
        visible = false,
        stopCount = 0,
        cost = 0.0,
        status = RollingTrailerYardRuleStatus.IDLE,
    )
}
