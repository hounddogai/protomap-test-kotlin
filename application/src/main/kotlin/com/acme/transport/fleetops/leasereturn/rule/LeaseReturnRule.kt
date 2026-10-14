package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lease return rule records of fleetops's lease return work, and the rules, stores, and calculations
// around them.

/** Where a lease return rule is in its life cycle. */
enum class LeaseReturnRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lease return rule as the lease return team keeps it. */
data class LeaseReturnRule(
    var code: String,
    var name: String,
    var segment: String,
    var loadKg: Double,
    var ceiling: Double,
    var createdAt: Int,
    var pallets: Int,
    var budget: Double,
    var expiresAt: Int,
    var unit: String,
    var status: LeaseReturnRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnRuleStatus): Boolean {
        when (status) {
            LeaseReturnRuleStatus.IDLE -> return next == LeaseReturnRuleStatus.WARMING || next == LeaseReturnRuleStatus.STOPPED
            LeaseReturnRuleStatus.WARMING -> return next == LeaseReturnRuleStatus.READY || next == LeaseReturnRuleStatus.STOPPED
            LeaseReturnRuleStatus.READY -> return next == LeaseReturnRuleStatus.STOPPED
            LeaseReturnRuleStatus.DRAINING -> {
                when (next) {
                    LeaseReturnRuleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnRuleStatus) {
        if (!canMoveTo(next)) {
            error("lease return rule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedVolume(): Double = round(budget * ceiling * 100) / 100

    fun snapshot(): LeaseReturnRule {
        return LeaseReturnRule(
            code = code,
            name = name,
            segment = segment,
            loadKg = loadKg,
            ceiling = ceiling,
            createdAt = createdAt,
            pallets = pallets,
            budget = budget,
            expiresAt = expiresAt,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a lease return rule in status idle. */
fun newLeaseReturnRule(code: String, name: String): LeaseReturnRule {
    return LeaseReturnRule(
        code = code,
        name = name,
        segment = "",
        loadKg = 0.0,
        ceiling = 0.0,
        createdAt = 0,
        pallets = 0,
        budget = 0.0,
        expiresAt = 0,
        unit = "",
        status = LeaseReturnRuleStatus.IDLE,
    )
}
