package com.acme.transport.linehaul.backhaul.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped backhaul rule records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a capped backhaul rule is in its life cycle. */
enum class CappedBackhaulRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a capped backhaul rule and its current status. */
data class CappedBackhaulRule(
    var code: String,
    var label: String,
    var limit: Int,
    var createdAt: Int,
    var description: String,
    var status: CappedBackhaulRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedBackhaulRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedBackhaulRuleStatus): Boolean {
        when (status) {
            CappedBackhaulRuleStatus.PROPOSED -> {
                when (next) {
                    CappedBackhaulRuleStatus.APPROVED, CappedBackhaulRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedBackhaulRuleStatus.APPROVED -> return next == CappedBackhaulRuleStatus.REJECTED || next == CappedBackhaulRuleStatus.WITHDRAWN
            CappedBackhaulRuleStatus.REJECTED -> return next == CappedBackhaulRuleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedBackhaulRuleStatus) {
        if (!canMoveTo(next)) {
            error("capped backhaul rule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedVolume(): Double = round((limit.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): CappedBackhaulRule {
        return CappedBackhaulRule(
            code = code,
            label = label,
            limit = limit,
            createdAt = createdAt,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped backhaul rule in status proposed. */
fun newCappedBackhaulRule(code: String, label: String): CappedBackhaulRule {
    return CappedBackhaulRule(
        code = code,
        label = label,
        limit = 0,
        createdAt = 0,
        description = "",
        status = CappedBackhaulRuleStatus.PROPOSED,
    )
}
