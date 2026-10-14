package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default duty period rule is in its life cycle. */
enum class DefaultDutyPeriodRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A default duty period rule as the duty period team keeps it. */
data class DefaultDutyPeriodRule(
    var code: String,
    var name: String,
    var version: Int,
    var checksum: String,
    var carrier: String,
    var vehicleId: String,
    var shiftCode: String,
    var quantity: Int,
    var rate: Double,
    var status: DefaultDutyPeriodRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDutyPeriodRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDutyPeriodRuleStatus): Boolean {
        when (status) {
            DefaultDutyPeriodRuleStatus.REQUESTED -> {
                when (next) {
                    DefaultDutyPeriodRuleStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultDutyPeriodRuleStatus.GRANTED -> {
                when (next) {
                    DefaultDutyPeriodRuleStatus.REVOKED, DefaultDutyPeriodRuleStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultDutyPeriodRuleStatus.REVOKED -> return next == DefaultDutyPeriodRuleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultDutyPeriodRuleStatus) {
        if (!canMoveTo(next)) {
            error("default duty period rule $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun adjustedValue(): Double = round((version.toDouble() + rate) * 100) / 100

    fun snapshot(): DefaultDutyPeriodRule {
        return DefaultDutyPeriodRule(
            code = code,
            name = name,
            version = version,
            checksum = checksum,
            carrier = carrier,
            vehicleId = vehicleId,
            shiftCode = shiftCode,
            quantity = quantity,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default duty period rule in status requested. */
fun newDefaultDutyPeriodRule(code: String, name: String): DefaultDutyPeriodRule {
    return DefaultDutyPeriodRule(
        code = code,
        name = name,
        version = 0,
        checksum = "",
        carrier = "",
        vehicleId = "",
        shiftCode = "",
        quantity = 0,
        rate = 0.0,
        status = DefaultDutyPeriodRuleStatus.REQUESTED,
    )
}
