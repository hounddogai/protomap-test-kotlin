package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.kit.text.normalize

/** Where a pending backhaul forecast is in its life cycle. */
enum class PendingBackhaulForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A pending backhaul forecast. Its code is unique within a tenant. */
data class PendingBackhaulForecast(
    var code: String,
    var title: String,
    var region: String,
    var contactEmail: String,
    var stopCount: Int,
    var shiftCode: String,
    var status: PendingBackhaulForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingBackhaulForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingBackhaulForecastStatus): Boolean {
        when (status) {
            PendingBackhaulForecastStatus.PROPOSED -> return next == PendingBackhaulForecastStatus.APPROVED || next == PendingBackhaulForecastStatus.WITHDRAWN
            PendingBackhaulForecastStatus.APPROVED -> return next == PendingBackhaulForecastStatus.REJECTED
            PendingBackhaulForecastStatus.REJECTED -> return next == PendingBackhaulForecastStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PendingBackhaulForecastStatus) {
        if (!canMoveTo(next)) {
            error("pending backhaul forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): PendingBackhaulForecast {
        return PendingBackhaulForecast(
            code = code,
            title = title,
            region = region,
            contactEmail = contactEmail,
            stopCount = stopCount,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending backhaul forecast in status proposed. */
fun newPendingBackhaulForecast(code: String, title: String): PendingBackhaulForecast {
    return PendingBackhaulForecast(
        code = code,
        title = title,
        region = "",
        contactEmail = "",
        stopCount = 0,
        shiftCode = "",
        status = PendingBackhaulForecastStatus.PROPOSED,
    )
}
