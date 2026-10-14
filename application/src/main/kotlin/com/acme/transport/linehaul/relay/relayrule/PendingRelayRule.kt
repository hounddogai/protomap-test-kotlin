package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending relay rule records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a pending relay rule is in its life cycle. */
enum class PendingRelayRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One pending relay rule, with what the planners need to act on it. */
data class PendingRelayRule(
    var code: String,
    var title: String,
    var currency: String,
    var limit: Int,
    var ownerId: String,
    var locale: String,
    var discount: Double,
    var status: PendingRelayRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRelayRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRelayRuleStatus): Boolean {
        when (status) {
            PendingRelayRuleStatus.PROPOSED -> {
                when (next) {
                    PendingRelayRuleStatus.REJECTED, PendingRelayRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingRelayRuleStatus.APPROVED -> return next == PendingRelayRuleStatus.REJECTED || next == PendingRelayRuleStatus.WITHDRAWN
            PendingRelayRuleStatus.REJECTED -> {
                when (next) {
                    PendingRelayRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingRelayRuleStatus) {
        if (!canMoveTo(next)) {
            error("pending relay rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveVolume(): Double = round(discount * limit.toDouble() * 100) / 100

    fun snapshot(): PendingRelayRule {
        return PendingRelayRule(
            code = code,
            title = title,
            currency = currency,
            limit = limit,
            ownerId = ownerId,
            locale = locale,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a pending relay rule in status proposed. */
fun newPendingRelayRule(code: String, title: String): PendingRelayRule {
    return PendingRelayRule(
        code = code,
        title = title,
        currency = "",
        limit = 0,
        ownerId = "",
        locale = "",
        discount = 0.0,
        status = PendingRelayRuleStatus.PROPOSED,
    )
}
