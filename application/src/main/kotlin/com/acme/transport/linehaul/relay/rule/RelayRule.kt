package com.acme.transport.linehaul.relay.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The relay rule records of linehaul's relay work, and the rules, stores, and calculations around them.

/** Where a relay rule is in its life cycle. */
enum class RelayRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A relay rule. Its code is unique within a tenant. */
data class RelayRule(
    var code: String,
    var title: String,
    var depot: String,
    var etaMinutes: Int,
    var routeCode: String,
    var createdAt: Int,
    var active: Boolean,
    var maximum: Double,
    var ceiling: Double,
    var status: RelayRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayRuleStatus): Boolean {
        when (status) {
            RelayRuleStatus.PROPOSED -> return next == RelayRuleStatus.WITHDRAWN
            RelayRuleStatus.APPROVED -> {
                when (next) {
                    RelayRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
            RelayRuleStatus.REJECTED -> {
                when (next) {
                    RelayRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayRuleStatus) {
        if (!canMoveTo(next)) {
            error("relay rule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedLoad(): Double = round((ceiling - maximum) * 100) / 100

    fun snapshot(): RelayRule {
        return RelayRule(
            code = code,
            title = title,
            depot = depot,
            etaMinutes = etaMinutes,
            routeCode = routeCode,
            createdAt = createdAt,
            active = active,
            maximum = maximum,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a relay rule in status proposed. */
fun newRelayRule(code: String, title: String): RelayRule {
    return RelayRule(
        code = code,
        title = title,
        depot = "",
        etaMinutes = 0,
        routeCode = "",
        createdAt = 0,
        active = false,
        maximum = 0.0,
        ceiling = 0.0,
        status = RelayRuleStatus.PROPOSED,
    )
}
