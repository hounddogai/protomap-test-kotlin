package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal tariff rule is in its life cycle. */
enum class SeasonalTariffRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One seasonal tariff rule, with what the planners need to act on it. */
data class SeasonalTariffRule(
    var code: String,
    var title: String,
    var category: String,
    var ownerId: String,
    var unit: String,
    var region: String,
    var cost: Double,
    var score: Double,
    var weight: Double,
    var capacity: Int,
    var status: SeasonalTariffRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTariffRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTariffRuleStatus): Boolean {
        when (status) {
            SeasonalTariffRuleStatus.IDLE -> {
                when (next) {
                    SeasonalTariffRuleStatus.WARMING -> return true
                }
                return false
            }
            SeasonalTariffRuleStatus.WARMING -> {
                when (next) {
                    SeasonalTariffRuleStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalTariffRuleStatus.READY -> {
                when (next) {
                    SeasonalTariffRuleStatus.DRAINING -> return true
                }
                return false
            }
            SeasonalTariffRuleStatus.DRAINING -> return next == SeasonalTariffRuleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SeasonalTariffRuleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal tariff rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveValue(): Double = round(weight * cost * 100) / 100

    fun snapshot(): SeasonalTariffRule {
        return SeasonalTariffRule(
            code = code,
            title = title,
            category = category,
            ownerId = ownerId,
            unit = unit,
            region = region,
            cost = cost,
            score = score,
            weight = weight,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal tariff rule in status idle. */
fun newSeasonalTariffRule(code: String, title: String): SeasonalTariffRule {
    return SeasonalTariffRule(
        code = code,
        title = title,
        category = "",
        ownerId = "",
        unit = "",
        region = "",
        cost = 0.0,
        score = 0.0,
        weight = 0.0,
        capacity = 0,
        status = SeasonalTariffRuleStatus.IDLE,
    )
}
