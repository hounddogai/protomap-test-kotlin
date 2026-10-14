package com.acme.transport.crewing.dutyperiod.target

import com.acme.transport.kit.text.normalize

/** Where a default duty period rollup is in its life cycle. */
enum class DefaultDutyPeriodRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A default duty period rollup as the duty period team keeps it. */
data class DefaultDutyPeriodRollup(
    var code: String,
    var name: String,
    var depot: String,
    var title: String,
    var visible: Boolean,
    var contactEmail: String,
    var amount: Double,
    var status: DefaultDutyPeriodRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDutyPeriodRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDutyPeriodRollupStatus): Boolean {
        when (status) {
            DefaultDutyPeriodRollupStatus.PROPOSED -> {
                when (next) {
                    DefaultDutyPeriodRollupStatus.APPROVED, DefaultDutyPeriodRollupStatus.REJECTED -> return true
                }
                return false
            }
            DefaultDutyPeriodRollupStatus.APPROVED -> return next == DefaultDutyPeriodRollupStatus.REJECTED || next == DefaultDutyPeriodRollupStatus.WITHDRAWN
            DefaultDutyPeriodRollupStatus.REJECTED -> return next == DefaultDutyPeriodRollupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefaultDutyPeriodRollupStatus) {
        if (!canMoveTo(next)) {
            error("default duty period rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = amount

    fun snapshot(): DefaultDutyPeriodRollup {
        return DefaultDutyPeriodRollup(
            code = code,
            name = name,
            depot = depot,
            title = title,
            visible = visible,
            contactEmail = contactEmail,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default duty period rollup in status proposed. */
fun newDefaultDutyPeriodRollup(code: String, name: String): DefaultDutyPeriodRollup {
    return DefaultDutyPeriodRollup(
        code = code,
        name = name,
        depot = "",
        title = "",
        visible = false,
        contactEmail = "",
        amount = 0.0,
        status = DefaultDutyPeriodRollupStatus.PROPOSED,
    )
}
