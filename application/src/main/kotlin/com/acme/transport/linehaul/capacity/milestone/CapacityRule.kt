package com.acme.transport.linehaul.capacity.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity rule is in its life cycle. */
enum class CapacityRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capacity rule as the capacity team keeps it. */
data class CapacityRule(
    var code: String,
    var name: String,
    var startsAt: Int,
    var updatedAt: Int,
    var revision: Int,
    var minimum: Double,
    var status: CapacityRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityRuleStatus): Boolean {
        when (status) {
            CapacityRuleStatus.CREATED -> return next == CapacityRuleStatus.PUBLISHED || next == CapacityRuleStatus.RETIRED
            CapacityRuleStatus.VALIDATED -> return next == CapacityRuleStatus.PUBLISHED || next == CapacityRuleStatus.RETIRED
            CapacityRuleStatus.PUBLISHED -> {
                when (next) {
                    CapacityRuleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityRuleStatus) {
        if (!canMoveTo(next)) {
            error("capacity rule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun projectedLoad(): Double = round((minimum + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): CapacityRule {
        return CapacityRule(
            code = code,
            name = name,
            startsAt = startsAt,
            updatedAt = updatedAt,
            revision = revision,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capacity rule in status created. */
fun newCapacityRule(code: String, name: String): CapacityRule {
    return CapacityRule(
        code = code,
        name = name,
        startsAt = 0,
        updatedAt = 0,
        revision = 0,
        minimum = 0.0,
        status = CapacityRuleStatus.CREATED,
    )
}
