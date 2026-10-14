package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched availability rule is in its life cycle. */
enum class BatchedAvailabilityRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A batched availability rule as the availability team keeps it. */
data class BatchedAvailabilityRule(
    var code: String,
    var name: String,
    var summary: String,
    var maximum: Double,
    var routeCode: String,
    var stopCount: Int,
    var quantity: Int,
    var cost: Double,
    var phone: String,
    var label: String,
    var locale: String,
    var status: BatchedAvailabilityRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAvailabilityRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAvailabilityRuleStatus): Boolean {
        when (status) {
            BatchedAvailabilityRuleStatus.DRAFT -> return next == BatchedAvailabilityRuleStatus.ACTIVE
            BatchedAvailabilityRuleStatus.PENDING -> return next == BatchedAvailabilityRuleStatus.SUSPENDED || next == BatchedAvailabilityRuleStatus.CLOSED
            BatchedAvailabilityRuleStatus.ACTIVE -> {
                when (next) {
                    BatchedAvailabilityRuleStatus.CLOSED -> return true
                }
                return false
            }
            BatchedAvailabilityRuleStatus.SUSPENDED -> return next == BatchedAvailabilityRuleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BatchedAvailabilityRuleStatus) {
        if (!canMoveTo(next)) {
            error("batched availability rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $name"

    fun netLoad(): Double = round(maximum * stopCount.toDouble() * 100) / 100

    fun snapshot(): BatchedAvailabilityRule {
        return BatchedAvailabilityRule(
            code = code,
            name = name,
            summary = summary,
            maximum = maximum,
            routeCode = routeCode,
            stopCount = stopCount,
            quantity = quantity,
            cost = cost,
            phone = phone,
            label = label,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a batched availability rule in status draft. */
fun newBatchedAvailabilityRule(code: String, name: String): BatchedAvailabilityRule {
    return BatchedAvailabilityRule(
        code = code,
        name = name,
        summary = "",
        maximum = 0.0,
        routeCode = "",
        stopCount = 0,
        quantity = 0,
        cost = 0.0,
        phone = "",
        label = "",
        locale = "",
        status = BatchedAvailabilityRuleStatus.DRAFT,
    )
}
