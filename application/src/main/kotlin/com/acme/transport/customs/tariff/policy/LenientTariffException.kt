package com.acme.transport.customs.tariff.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient tariff exception is in its life cycle. */
enum class LenientTariffExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lenient tariff exception as the tariff team keeps it. */
data class LenientTariffException(
    var code: String,
    var label: String,
    var lane: String,
    var batchSize: Int,
    var margin: Double,
    var durationMinutes: Int,
    var ownerId: String,
    var routeCode: String,
    var status: LenientTariffExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientTariffExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientTariffExceptionStatus): Boolean {
        when (status) {
            LenientTariffExceptionStatus.IDLE -> {
                when (next) {
                    LenientTariffExceptionStatus.DRAINING -> return true
                }
                return false
            }
            LenientTariffExceptionStatus.WARMING -> return next == LenientTariffExceptionStatus.READY || next == LenientTariffExceptionStatus.STOPPED
            LenientTariffExceptionStatus.READY -> return next == LenientTariffExceptionStatus.DRAINING || next == LenientTariffExceptionStatus.STOPPED
            LenientTariffExceptionStatus.DRAINING -> return next == LenientTariffExceptionStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LenientTariffExceptionStatus) {
        if (!canMoveTo(next)) {
            error("lenient tariff exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedTotal(): Double = round((batchSize.toDouble() - margin) * 100) / 100

    fun snapshot(): LenientTariffException {
        return LenientTariffException(
            code = code,
            label = label,
            lane = lane,
            batchSize = batchSize,
            margin = margin,
            durationMinutes = durationMinutes,
            ownerId = ownerId,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lenient tariff exception in status idle. */
fun newLenientTariffException(code: String, label: String): LenientTariffException {
    return LenientTariffException(
        code = code,
        label = label,
        lane = "",
        batchSize = 0,
        margin = 0.0,
        durationMinutes = 0,
        ownerId = "",
        routeCode = "",
        status = LenientTariffExceptionStatus.IDLE,
    )
}
