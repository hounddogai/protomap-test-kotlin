package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.kit.text.normalize

/** Where a default fuel surcharge rule is in its life cycle. */
enum class DefaultFuelSurchargeRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A default fuel surcharge rule as the fuel surcharge team keeps it. */
data class DefaultFuelSurchargeRule(
    var code: String,
    var label: String,
    var country: String,
    var reviewer: String,
    var region: String,
    var visible: Boolean,
    var depot: String,
    var ownerId: String,
    var graceDays: Int,
    var status: DefaultFuelSurchargeRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultFuelSurchargeRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultFuelSurchargeRuleStatus): Boolean {
        when (status) {
            DefaultFuelSurchargeRuleStatus.REQUESTED -> {
                when (next) {
                    DefaultFuelSurchargeRuleStatus.REVOKED, DefaultFuelSurchargeRuleStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultFuelSurchargeRuleStatus.GRANTED -> {
                when (next) {
                    DefaultFuelSurchargeRuleStatus.REVOKED, DefaultFuelSurchargeRuleStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultFuelSurchargeRuleStatus.REVOKED -> return next == DefaultFuelSurchargeRuleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultFuelSurchargeRuleStatus) {
        if (!canMoveTo(next)) {
            error("default fuel surcharge rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): DefaultFuelSurchargeRule {
        return DefaultFuelSurchargeRule(
            code = code,
            label = label,
            country = country,
            reviewer = reviewer,
            region = region,
            visible = visible,
            depot = depot,
            ownerId = ownerId,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a default fuel surcharge rule in status requested. */
fun newDefaultFuelSurchargeRule(code: String, label: String): DefaultFuelSurchargeRule {
    return DefaultFuelSurchargeRule(
        code = code,
        label = label,
        country = "",
        reviewer = "",
        region = "",
        visible = false,
        depot = "",
        ownerId = "",
        graceDays = 0,
        status = DefaultFuelSurchargeRuleStatus.REQUESTED,
    )
}
