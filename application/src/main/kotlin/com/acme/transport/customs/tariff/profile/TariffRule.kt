package com.acme.transport.customs.tariff.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff rule is in its life cycle. */
enum class TariffRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a tariff rule and its current status. */
data class TariffRule(
    var code: String,
    var title: String,
    var shiftCode: String,
    var driverId: String,
    var capacity: Int,
    var locale: String,
    var approved: Boolean,
    var minimum: Double,
    var status: TariffRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffRuleStatus): Boolean {
        when (status) {
            TariffRuleStatus.DRAFT -> return next == TariffRuleStatus.SUSPENDED || next == TariffRuleStatus.CLOSED
            TariffRuleStatus.PENDING -> return next == TariffRuleStatus.ACTIVE || next == TariffRuleStatus.CLOSED
            TariffRuleStatus.ACTIVE -> {
                when (next) {
                    TariffRuleStatus.SUSPENDED, TariffRuleStatus.CLOSED -> return true
                }
                return false
            }
            TariffRuleStatus.SUSPENDED -> return next == TariffRuleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TariffRuleStatus) {
        if (!canMoveTo(next)) {
            error("tariff rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round((minimum + capacity.toDouble()) * 100) / 100

    fun snapshot(): TariffRule {
        return TariffRule(
            code = code,
            title = title,
            shiftCode = shiftCode,
            driverId = driverId,
            capacity = capacity,
            locale = locale,
            approved = approved,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tariff rule in status draft. */
fun newTariffRule(code: String, title: String): TariffRule {
    return TariffRule(
        code = code,
        title = title,
        shiftCode = "",
        driverId = "",
        capacity = 0,
        locale = "",
        approved = false,
        minimum = 0.0,
        status = TariffRuleStatus.DRAFT,
    )
}
