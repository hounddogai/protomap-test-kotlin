package com.acme.transport.customs.broker.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker rule is in its life cycle. */
enum class BrokerRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A broker rule. Its code is unique within a tenant. */
data class BrokerRule(
    var code: String,
    var name: String,
    var weight: Double,
    var floorValue: Double,
    var visible: Boolean,
    var lane: String,
    var pallets: Int,
    var contactEmail: String,
    var budget: Double,
    var status: BrokerRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerRuleStatus): Boolean {
        when (status) {
            BrokerRuleStatus.DRAFT -> {
                when (next) {
                    BrokerRuleStatus.SUSPENDED, BrokerRuleStatus.CLOSED -> return true
                }
                return false
            }
            BrokerRuleStatus.PENDING -> return next == BrokerRuleStatus.SUSPENDED
            BrokerRuleStatus.ACTIVE -> {
                when (next) {
                    BrokerRuleStatus.SUSPENDED, BrokerRuleStatus.CLOSED -> return true
                }
                return false
            }
            BrokerRuleStatus.SUSPENDED -> return next == BrokerRuleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BrokerRuleStatus) {
        if (!canMoveTo(next)) {
            error("broker rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netLoad(): Double = round(floorValue * budget * 100) / 100

    fun snapshot(): BrokerRule {
        return BrokerRule(
            code = code,
            name = name,
            weight = weight,
            floorValue = floorValue,
            visible = visible,
            lane = lane,
            pallets = pallets,
            contactEmail = contactEmail,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a broker rule in status draft. */
fun newBrokerRule(code: String, name: String): BrokerRule {
    return BrokerRule(
        code = code,
        name = name,
        weight = 0.0,
        floorValue = 0.0,
        visible = false,
        lane = "",
        pallets = 0,
        contactEmail = "",
        budget = 0.0,
        status = BrokerRuleStatus.DRAFT,
    )
}
