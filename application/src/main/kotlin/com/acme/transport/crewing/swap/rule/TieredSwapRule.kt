package com.acme.transport.crewing.swap.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered swap rule records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a tiered swap rule is in its life cycle. */
enum class TieredSwapRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tiered swap rule, with what the planners need to act on it. */
data class TieredSwapRule(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var expiresAt: Int,
    var region: String,
    var updatedAt: Int,
    var segment: String,
    var batchSize: Int,
    var status: TieredSwapRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredSwapRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredSwapRuleStatus): Boolean {
        when (status) {
            TieredSwapRuleStatus.IDLE -> {
                when (next) {
                    TieredSwapRuleStatus.READY, TieredSwapRuleStatus.DRAINING -> return true
                }
                return false
            }
            TieredSwapRuleStatus.WARMING -> return next == TieredSwapRuleStatus.DRAINING
            TieredSwapRuleStatus.READY -> return next == TieredSwapRuleStatus.DRAINING || next == TieredSwapRuleStatus.STOPPED
            TieredSwapRuleStatus.DRAINING -> {
                when (next) {
                    TieredSwapRuleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredSwapRuleStatus) {
        if (!canMoveTo(next)) {
            error("tiered swap rule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered swap rule"} $code"

    fun weightedVolume(): Double = round((expiresAt.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): TieredSwapRule {
        return TieredSwapRule(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            expiresAt = expiresAt,
            region = region,
            updatedAt = updatedAt,
            segment = segment,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tiered swap rule in status idle. */
fun newTieredSwapRule(code: String, title: String): TieredSwapRule {
    return TieredSwapRule(
        code = code,
        title = title,
        etaMinutes = 0,
        expiresAt = 0,
        region = "",
        updatedAt = 0,
        segment = "",
        batchSize = 0,
        status = TieredSwapRuleStatus.IDLE,
    )
}
