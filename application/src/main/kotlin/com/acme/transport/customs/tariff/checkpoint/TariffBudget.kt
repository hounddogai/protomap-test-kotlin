package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff budget is in its life cycle. */
enum class TariffBudgetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tariff budget as the tariff team keeps it. */
data class TariffBudget(
    var code: String,
    var name: String,
    var currency: String,
    var locale: String,
    var createdAt: Int,
    var unit: String,
    var stopCount: Int,
    var lane: String,
    var status: TariffBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffBudgetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffBudgetStatus): Boolean {
        when (status) {
            TariffBudgetStatus.NEW -> return next == TariffBudgetStatus.FAILED
            TariffBudgetStatus.QUEUED -> return next == TariffBudgetStatus.DONE
            TariffBudgetStatus.RUNNING -> return next == TariffBudgetStatus.DONE || next == TariffBudgetStatus.FAILED
            TariffBudgetStatus.DONE -> return next == TariffBudgetStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TariffBudgetStatus) {
        if (!canMoveTo(next)) {
            error("tariff budget $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedTotal(): Double = round(stopCount.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): TariffBudget {
        return TariffBudget(
            code = code,
            name = name,
            currency = currency,
            locale = locale,
            createdAt = createdAt,
            unit = unit,
            stopCount = stopCount,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tariff budget in status new. */
fun newTariffBudget(code: String, name: String): TariffBudget {
    return TariffBudget(
        code = code,
        name = name,
        currency = "",
        locale = "",
        createdAt = 0,
        unit = "",
        stopCount = 0,
        lane = "",
        status = TariffBudgetStatus.NEW,
    )
}
