package com.acme.transport.fleetops.tire.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire rule is in its life cycle. */
enum class TireRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One tire rule, with what the planners need to act on it. */
data class TireRule(
    var code: String,
    var title: String,
    var dock: String,
    var category: String,
    var capacity: Int,
    var region: String,
    var pallets: Int,
    var status: TireRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TireRuleStatus): Boolean {
        when (status) {
            TireRuleStatus.PROPOSED -> {
                when (next) {
                    TireRuleStatus.APPROVED -> return true
                }
                return false
            }
            TireRuleStatus.APPROVED -> return next == TireRuleStatus.REJECTED || next == TireRuleStatus.WITHDRAWN
            TireRuleStatus.REJECTED -> return next == TireRuleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TireRuleStatus) {
        if (!canMoveTo(next)) {
            error("tire rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tire rule"} $code"

    fun weightedLoad(): Double = round((pallets.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): TireRule {
        return TireRule(
            code = code,
            title = title,
            dock = dock,
            category = category,
            capacity = capacity,
            region = region,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tire rule in status proposed. */
fun newTireRule(code: String, title: String): TireRule {
    return TireRule(
        code = code,
        title = title,
        dock = "",
        category = "",
        capacity = 0,
        region = "",
        pallets = 0,
        status = TireRuleStatus.PROPOSED,
    )
}
