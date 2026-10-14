package com.acme.transport.network.node.nodescore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node rule is in its life cycle. */
enum class NodeRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One node rule, with what the planners need to act on it. */
data class NodeRule(
    var code: String,
    var label: String,
    var contactEmail: String,
    var fuelLiters: Double,
    var startsAt: Int,
    var phone: String,
    var floorValue: Double,
    var pinned: Boolean,
    var timezone: String,
    var loadKg: Double,
    var status: NodeRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeRuleStatus): Boolean {
        when (status) {
            NodeRuleStatus.CREATED -> return next == NodeRuleStatus.VALIDATED || next == NodeRuleStatus.RETIRED
            NodeRuleStatus.VALIDATED -> {
                when (next) {
                    NodeRuleStatus.PUBLISHED, NodeRuleStatus.RETIRED -> return true
                }
                return false
            }
            NodeRuleStatus.PUBLISHED -> return next == NodeRuleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: NodeRuleStatus) {
        if (!canMoveTo(next)) {
            error("node rule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedValue(): Double = round((floorValue - loadKg) * 100) / 100

    fun snapshot(): NodeRule {
        return NodeRule(
            code = code,
            label = label,
            contactEmail = contactEmail,
            fuelLiters = fuelLiters,
            startsAt = startsAt,
            phone = phone,
            floorValue = floorValue,
            pinned = pinned,
            timezone = timezone,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a node rule in status created. */
fun newNodeRule(code: String, label: String): NodeRule {
    return NodeRule(
        code = code,
        label = label,
        contactEmail = "",
        fuelLiters = 0.0,
        startsAt = 0,
        phone = "",
        floorValue = 0.0,
        pinned = false,
        timezone = "",
        loadKg = 0.0,
        status = NodeRuleStatus.CREATED,
    )
}
