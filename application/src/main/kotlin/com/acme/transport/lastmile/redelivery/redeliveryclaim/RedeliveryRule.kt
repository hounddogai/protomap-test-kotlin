package com.acme.transport.lastmile.redelivery.redeliveryclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery rule is in its life cycle. */
enum class RedeliveryRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One redelivery rule, with what the planners need to act on it. */
data class RedeliveryRule(
    var code: String,
    var title: String,
    var version: Int,
    var reviewer: String,
    var notes: String,
    var createdAt: Int,
    var amount: Double,
    var depot: String,
    var quantity: Int,
    var checksum: String,
    var status: RedeliveryRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryRuleStatus): Boolean {
        when (status) {
            RedeliveryRuleStatus.IDLE -> return next == RedeliveryRuleStatus.STOPPED
            RedeliveryRuleStatus.WARMING -> {
                when (next) {
                    RedeliveryRuleStatus.READY, RedeliveryRuleStatus.DRAINING -> return true
                }
                return false
            }
            RedeliveryRuleStatus.READY -> return next == RedeliveryRuleStatus.STOPPED
            RedeliveryRuleStatus.DRAINING -> return next == RedeliveryRuleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RedeliveryRuleStatus) {
        if (!canMoveTo(next)) {
            error("redelivery rule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"redelivery rule"} $code"

    fun projectedValue(): Double = round((quantity.toDouble() + amount) * 100) / 100

    fun snapshot(): RedeliveryRule {
        return RedeliveryRule(
            code = code,
            title = title,
            version = version,
            reviewer = reviewer,
            notes = notes,
            createdAt = createdAt,
            amount = amount,
            depot = depot,
            quantity = quantity,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a redelivery rule in status idle. */
fun newRedeliveryRule(code: String, title: String): RedeliveryRule {
    return RedeliveryRule(
        code = code,
        title = title,
        version = 0,
        reviewer = "",
        notes = "",
        createdAt = 0,
        amount = 0.0,
        depot = "",
        quantity = 0,
        checksum = "",
        status = RedeliveryRuleStatus.IDLE,
    )
}
