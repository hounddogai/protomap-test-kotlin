package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.text.normalize

/** Where a capped capacity forecast is in its life cycle. */
enum class CappedCapacityForecastStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a capped capacity forecast and its current status. */
data class CappedCapacityForecast(
    var code: String,
    var name: String,
    var archived: Boolean,
    var currency: String,
    var description: String,
    var amount: Double,
    var status: CappedCapacityForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCapacityForecastStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCapacityForecastStatus): Boolean {
        when (status) {
            CappedCapacityForecastStatus.REQUESTED -> {
                when (next) {
                    CappedCapacityForecastStatus.GRANTED, CappedCapacityForecastStatus.EXPIRED -> return true
                }
                return false
            }
            CappedCapacityForecastStatus.GRANTED -> return next == CappedCapacityForecastStatus.REVOKED || next == CappedCapacityForecastStatus.EXPIRED
            CappedCapacityForecastStatus.REVOKED -> {
                when (next) {
                    CappedCapacityForecastStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedCapacityForecastStatus) {
        if (!canMoveTo(next)) {
            error("capped capacity forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped capacity forecast"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): CappedCapacityForecast {
        return CappedCapacityForecast(
            code = code,
            name = name,
            archived = archived,
            currency = currency,
            description = description,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped capacity forecast in status requested. */
fun newCappedCapacityForecast(code: String, name: String): CappedCapacityForecast {
    return CappedCapacityForecast(
        code = code,
        name = name,
        archived = false,
        currency = "",
        description = "",
        amount = 0.0,
        status = CappedCapacityForecastStatus.REQUESTED,
    )
}
