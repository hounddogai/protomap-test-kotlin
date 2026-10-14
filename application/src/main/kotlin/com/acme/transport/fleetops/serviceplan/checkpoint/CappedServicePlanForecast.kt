package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.kit.text.normalize

/** Where a capped service plan forecast is in its life cycle. */
enum class CappedServicePlanForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a capped service plan forecast and its current status. */
data class CappedServicePlanForecast(
    var code: String,
    var title: String,
    var batchSize: Int,
    var contactEmail: String,
    var shiftCode: String,
    var dock: String,
    var status: CappedServicePlanForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedServicePlanForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedServicePlanForecastStatus): Boolean {
        when (status) {
            CappedServicePlanForecastStatus.PROPOSED -> {
                when (next) {
                    CappedServicePlanForecastStatus.REJECTED, CappedServicePlanForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedServicePlanForecastStatus.APPROVED -> return next == CappedServicePlanForecastStatus.REJECTED || next == CappedServicePlanForecastStatus.WITHDRAWN
            CappedServicePlanForecastStatus.REJECTED -> return next == CappedServicePlanForecastStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedServicePlanForecastStatus) {
        if (!canMoveTo(next)) {
            error("capped service plan forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped service plan forecast"} $code"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): CappedServicePlanForecast {
        return CappedServicePlanForecast(
            code = code,
            title = title,
            batchSize = batchSize,
            contactEmail = contactEmail,
            shiftCode = shiftCode,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped service plan forecast in status proposed. */
fun newCappedServicePlanForecast(code: String, title: String): CappedServicePlanForecast {
    return CappedServicePlanForecast(
        code = code,
        title = title,
        batchSize = 0,
        contactEmail = "",
        shiftCode = "",
        dock = "",
        status = CappedServicePlanForecastStatus.PROPOSED,
    )
}
