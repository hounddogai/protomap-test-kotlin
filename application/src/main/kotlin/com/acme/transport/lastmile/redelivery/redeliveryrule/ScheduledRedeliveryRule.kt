package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.kit.text.normalize

// The scheduled redelivery rule records of lastmile's redelivery work, and the rules, stores, and
// calculations around them.

/** Where a scheduled redelivery rule is in its life cycle. */
enum class ScheduledRedeliveryRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scheduled redelivery rule, with what the planners need to act on it. */
data class ScheduledRedeliveryRule(
    var code: String,
    var name: String,
    var shiftCode: String,
    var vehicleId: String,
    var revision: Int,
    var status: ScheduledRedeliveryRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRedeliveryRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRedeliveryRuleStatus): Boolean {
        when (status) {
            ScheduledRedeliveryRuleStatus.CREATED -> return next == ScheduledRedeliveryRuleStatus.VALIDATED
            ScheduledRedeliveryRuleStatus.VALIDATED -> return next == ScheduledRedeliveryRuleStatus.RETIRED
            ScheduledRedeliveryRuleStatus.PUBLISHED -> {
                when (next) {
                    ScheduledRedeliveryRuleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRedeliveryRuleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled redelivery rule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): ScheduledRedeliveryRule {
        return ScheduledRedeliveryRule(
            code = code,
            name = name,
            shiftCode = shiftCode,
            vehicleId = vehicleId,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled redelivery rule in status created. */
fun newScheduledRedeliveryRule(code: String, name: String): ScheduledRedeliveryRule {
    return ScheduledRedeliveryRule(
        code = code,
        name = name,
        shiftCode = "",
        vehicleId = "",
        revision = 0,
        status = ScheduledRedeliveryRuleStatus.CREATED,
    )
}
