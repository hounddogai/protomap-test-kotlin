package com.acme.transport.fleetops.utilization.assignment

import com.acme.transport.kit.text.normalize

/** Where a weighted utilization rule is in its life cycle. */
enum class WeightedUtilizationRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted utilization rule. Its code is unique within a tenant. */
data class WeightedUtilizationRule(
    var code: String,
    var name: String,
    var dock: String,
    var quantity: Int,
    var phone: String,
    var segment: String,
    var status: WeightedUtilizationRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedUtilizationRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedUtilizationRuleStatus): Boolean {
        when (status) {
            WeightedUtilizationRuleStatus.DRAFT -> return next == WeightedUtilizationRuleStatus.PENDING || next == WeightedUtilizationRuleStatus.ACTIVE
            WeightedUtilizationRuleStatus.PENDING -> {
                when (next) {
                    WeightedUtilizationRuleStatus.SUSPENDED, WeightedUtilizationRuleStatus.CLOSED -> return true
                }
                return false
            }
            WeightedUtilizationRuleStatus.ACTIVE -> {
                when (next) {
                    WeightedUtilizationRuleStatus.CLOSED -> return true
                }
                return false
            }
            WeightedUtilizationRuleStatus.SUSPENDED -> return next == WeightedUtilizationRuleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: WeightedUtilizationRuleStatus) {
        if (!canMoveTo(next)) {
            error("weighted utilization rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): WeightedUtilizationRule {
        return WeightedUtilizationRule(
            code = code,
            name = name,
            dock = dock,
            quantity = quantity,
            phone = phone,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted utilization rule in status draft. */
fun newWeightedUtilizationRule(code: String, name: String): WeightedUtilizationRule {
    return WeightedUtilizationRule(
        code = code,
        name = name,
        dock = "",
        quantity = 0,
        phone = "",
        segment = "",
        status = WeightedUtilizationRuleStatus.DRAFT,
    )
}
