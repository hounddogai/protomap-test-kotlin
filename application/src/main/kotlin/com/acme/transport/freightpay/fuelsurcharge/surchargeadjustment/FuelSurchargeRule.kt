package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge rule is in its life cycle. */
enum class FuelSurchargeRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fuel surcharge rule. Its code is unique within a tenant. */
data class FuelSurchargeRule(
    var code: String,
    var label: String,
    var stopCount: Int,
    var vehicleId: String,
    var maximum: Double,
    var status: FuelSurchargeRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeRuleStatus): Boolean {
        when (status) {
            FuelSurchargeRuleStatus.REQUESTED -> {
                when (next) {
                    FuelSurchargeRuleStatus.GRANTED, FuelSurchargeRuleStatus.REVOKED -> return true
                }
                return false
            }
            FuelSurchargeRuleStatus.GRANTED -> {
                when (next) {
                    FuelSurchargeRuleStatus.REVOKED -> return true
                }
                return false
            }
            FuelSurchargeRuleStatus.REVOKED -> return next == FuelSurchargeRuleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeRuleStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fuel surcharge rule"} $code"

    fun weightedExposure(): Double = round((stopCount.toDouble() - maximum) * 100) / 100

    fun snapshot(): FuelSurchargeRule {
        return FuelSurchargeRule(
            code = code,
            label = label,
            stopCount = stopCount,
            vehicleId = vehicleId,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fuel surcharge rule in status requested. */
fun newFuelSurchargeRule(code: String, label: String): FuelSurchargeRule {
    return FuelSurchargeRule(
        code = code,
        label = label,
        stopCount = 0,
        vehicleId = "",
        maximum = 0.0,
        status = FuelSurchargeRuleStatus.REQUESTED,
    )
}
