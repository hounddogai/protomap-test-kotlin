package com.acme.transport.customs.tariff.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default tariff estimate is in its life cycle. */
enum class DefaultTariffEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One default tariff estimate, with what the planners need to act on it. */
data class DefaultTariffEstimate(
    var code: String,
    var name: String,
    var title: String,
    var limit: Int,
    var etaMinutes: Int,
    var amount: Double,
    var quantity: Int,
    var lane: String,
    var region: String,
    var cost: Double,
    var windowDays: Int,
    var status: DefaultTariffEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTariffEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTariffEstimateStatus): Boolean {
        when (status) {
            DefaultTariffEstimateStatus.OPEN -> {
                when (next) {
                    DefaultTariffEstimateStatus.REOPENED -> return true
                }
                return false
            }
            DefaultTariffEstimateStatus.IN_REVIEW -> {
                when (next) {
                    DefaultTariffEstimateStatus.RESOLVED, DefaultTariffEstimateStatus.REOPENED -> return true
                }
                return false
            }
            DefaultTariffEstimateStatus.RESOLVED -> return next == DefaultTariffEstimateStatus.REOPENED || next == DefaultTariffEstimateStatus.ARCHIVED
            DefaultTariffEstimateStatus.REOPENED -> return next == DefaultTariffEstimateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DefaultTariffEstimateStatus) {
        if (!canMoveTo(next)) {
            error("default tariff estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedValue(): Double = round((windowDays.toDouble() - cost) * 100) / 100

    fun snapshot(): DefaultTariffEstimate {
        return DefaultTariffEstimate(
            code = code,
            name = name,
            title = title,
            limit = limit,
            etaMinutes = etaMinutes,
            amount = amount,
            quantity = quantity,
            lane = lane,
            region = region,
            cost = cost,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default tariff estimate in status open. */
fun newDefaultTariffEstimate(code: String, name: String): DefaultTariffEstimate {
    return DefaultTariffEstimate(
        code = code,
        name = name,
        title = "",
        limit = 0,
        etaMinutes = 0,
        amount = 0.0,
        quantity = 0,
        lane = "",
        region = "",
        cost = 0.0,
        windowDays = 0,
        status = DefaultTariffEstimateStatus.OPEN,
    )
}
