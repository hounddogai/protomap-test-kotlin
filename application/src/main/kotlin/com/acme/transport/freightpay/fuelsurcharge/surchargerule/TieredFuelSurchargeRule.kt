package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered fuel surcharge rule records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a tiered fuel surcharge rule is in its life cycle. */
enum class TieredFuelSurchargeRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tiered fuel surcharge rule and its current status. */
data class TieredFuelSurchargeRule(
    var code: String,
    var title: String,
    var channel: String,
    var summary: String,
    var revision: Int,
    var graceDays: Int,
    var amount: Double,
    var notes: String,
    var cost: Double,
    var lane: String,
    var budget: Double,
    var status: TieredFuelSurchargeRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFuelSurchargeRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFuelSurchargeRuleStatus): Boolean {
        when (status) {
            TieredFuelSurchargeRuleStatus.REQUESTED -> return next == TieredFuelSurchargeRuleStatus.GRANTED || next == TieredFuelSurchargeRuleStatus.REVOKED
            TieredFuelSurchargeRuleStatus.GRANTED -> {
                when (next) {
                    TieredFuelSurchargeRuleStatus.REVOKED, TieredFuelSurchargeRuleStatus.EXPIRED -> return true
                }
                return false
            }
            TieredFuelSurchargeRuleStatus.REVOKED -> {
                when (next) {
                    TieredFuelSurchargeRuleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredFuelSurchargeRuleStatus) {
        if (!canMoveTo(next)) {
            error("tiered fuel surcharge rule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"tiered fuel surcharge rule"} $code"

    fun weightedValue(): Double = round(budget * graceDays.toDouble() * 100) / 100

    fun snapshot(): TieredFuelSurchargeRule {
        return TieredFuelSurchargeRule(
            code = code,
            title = title,
            channel = channel,
            summary = summary,
            revision = revision,
            graceDays = graceDays,
            amount = amount,
            notes = notes,
            cost = cost,
            lane = lane,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tiered fuel surcharge rule in status requested. */
fun newTieredFuelSurchargeRule(code: String, title: String): TieredFuelSurchargeRule {
    return TieredFuelSurchargeRule(
        code = code,
        title = title,
        channel = "",
        summary = "",
        revision = 0,
        graceDays = 0,
        amount = 0.0,
        notes = "",
        cost = 0.0,
        lane = "",
        budget = 0.0,
        status = TieredFuelSurchargeRuleStatus.REQUESTED,
    )
}
