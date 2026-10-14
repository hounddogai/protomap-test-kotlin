package com.acme.transport.freightpay.fuelsurcharge.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional fuel surcharge rule records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a regional fuel surcharge rule is in its life cycle. */
enum class RegionalFuelSurchargeRuleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A regional fuel surcharge rule. Its code is unique within a tenant. */
data class RegionalFuelSurchargeRule(
    var code: String,
    var label: String,
    var country: String,
    var carrier: String,
    var archived: Boolean,
    var revision: Int,
    var minimum: Double,
    var active: Boolean,
    var externalRef: String,
    var status: RegionalFuelSurchargeRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFuelSurchargeRuleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFuelSurchargeRuleStatus): Boolean {
        when (status) {
            RegionalFuelSurchargeRuleStatus.NEW -> {
                when (next) {
                    RegionalFuelSurchargeRuleStatus.FAILED -> return true
                }
                return false
            }
            RegionalFuelSurchargeRuleStatus.QUEUED -> return next == RegionalFuelSurchargeRuleStatus.FAILED
            RegionalFuelSurchargeRuleStatus.RUNNING -> return next == RegionalFuelSurchargeRuleStatus.DONE || next == RegionalFuelSurchargeRuleStatus.FAILED
            RegionalFuelSurchargeRuleStatus.DONE -> return next == RegionalFuelSurchargeRuleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalFuelSurchargeRuleStatus) {
        if (!canMoveTo(next)) {
            error("regional fuel surcharge rule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"regional fuel surcharge rule"} $code"

    fun netTotal(): Double = round(minimum * revision.toDouble() * 100) / 100

    fun snapshot(): RegionalFuelSurchargeRule {
        return RegionalFuelSurchargeRule(
            code = code,
            label = label,
            country = country,
            carrier = carrier,
            archived = archived,
            revision = revision,
            minimum = minimum,
            active = active,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a regional fuel surcharge rule in status new. */
fun newRegionalFuelSurchargeRule(code: String, label: String): RegionalFuelSurchargeRule {
    return RegionalFuelSurchargeRule(
        code = code,
        label = label,
        country = "",
        carrier = "",
        archived = false,
        revision = 0,
        minimum = 0.0,
        active = false,
        externalRef = "",
        status = RegionalFuelSurchargeRuleStatus.NEW,
    )
}
