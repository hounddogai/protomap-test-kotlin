package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area rule is in its life cycle. */
enum class ServiceAreaRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One service area rule, with what the planners need to act on it. */
data class ServiceAreaRule(
    var code: String,
    var label: String,
    var graceDays: Int,
    var retries: Int,
    var unit: String,
    var shiftCode: String,
    var margin: Double,
    var visible: Boolean,
    var dock: String,
    var vehicleId: String,
    var amount: Double,
    var status: ServiceAreaRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaRuleStatus): Boolean {
        when (status) {
            ServiceAreaRuleStatus.DRAFT -> return next == ServiceAreaRuleStatus.SUSPENDED
            ServiceAreaRuleStatus.PENDING -> {
                when (next) {
                    ServiceAreaRuleStatus.ACTIVE, ServiceAreaRuleStatus.CLOSED -> return true
                }
                return false
            }
            ServiceAreaRuleStatus.ACTIVE -> return next == ServiceAreaRuleStatus.CLOSED
            ServiceAreaRuleStatus.SUSPENDED -> {
                when (next) {
                    ServiceAreaRuleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaRuleStatus) {
        if (!canMoveTo(next)) {
            error("service area rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round((amount + margin) * 100) / 100

    fun snapshot(): ServiceAreaRule {
        return ServiceAreaRule(
            code = code,
            label = label,
            graceDays = graceDays,
            retries = retries,
            unit = unit,
            shiftCode = shiftCode,
            margin = margin,
            visible = visible,
            dock = dock,
            vehicleId = vehicleId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a service area rule in status draft. */
fun newServiceAreaRule(code: String, label: String): ServiceAreaRule {
    return ServiceAreaRule(
        code = code,
        label = label,
        graceDays = 0,
        retries = 0,
        unit = "",
        shiftCode = "",
        margin = 0.0,
        visible = false,
        dock = "",
        vehicleId = "",
        amount = 0.0,
        status = ServiceAreaRuleStatus.DRAFT,
    )
}
