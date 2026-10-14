package com.acme.transport.lastmile.drop.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary drop rule records of lastmile's drop work, and the rules, stores, and calculations around them.

/** Where a primary drop rule is in its life cycle. */
enum class PrimaryDropRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One primary drop rule, with what the planners need to act on it. */
data class PrimaryDropRule(
    var code: String,
    var label: String,
    var ceiling: Double,
    var maximum: Double,
    var score: Double,
    var checksum: String,
    var updatedAt: Int,
    var status: PrimaryDropRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDropRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDropRuleStatus): Boolean {
        when (status) {
            PrimaryDropRuleStatus.IDLE -> {
                when (next) {
                    PrimaryDropRuleStatus.WARMING, PrimaryDropRuleStatus.DRAINING -> return true
                }
                return false
            }
            PrimaryDropRuleStatus.WARMING -> {
                when (next) {
                    PrimaryDropRuleStatus.DRAINING -> return true
                }
                return false
            }
            PrimaryDropRuleStatus.READY -> return next == PrimaryDropRuleStatus.DRAINING || next == PrimaryDropRuleStatus.STOPPED
            PrimaryDropRuleStatus.DRAINING -> {
                when (next) {
                    PrimaryDropRuleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryDropRuleStatus) {
        if (!canMoveTo(next)) {
            error("primary drop rule $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveExposure(): Double = round((maximum - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryDropRule {
        return PrimaryDropRule(
            code = code,
            label = label,
            ceiling = ceiling,
            maximum = maximum,
            score = score,
            checksum = checksum,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary drop rule in status idle. */
fun newPrimaryDropRule(code: String, label: String): PrimaryDropRule {
    return PrimaryDropRule(
        code = code,
        label = label,
        ceiling = 0.0,
        maximum = 0.0,
        score = 0.0,
        checksum = "",
        updatedAt = 0,
        status = PrimaryDropRuleStatus.IDLE,
    )
}
