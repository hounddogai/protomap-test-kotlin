package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.kit.text.normalize

/** Where a tariff window is in its life cycle. */
enum class TariffWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One tariff window, with what the planners need to act on it. */
data class TariffWindow(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var shiftCode: String,
    var country: String,
    var status: TariffWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffWindowStatus): Boolean {
        when (status) {
            TariffWindowStatus.PROPOSED -> {
                when (next) {
                    TariffWindowStatus.APPROVED, TariffWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
            TariffWindowStatus.APPROVED -> return next == TariffWindowStatus.REJECTED
            TariffWindowStatus.REJECTED -> return next == TariffWindowStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TariffWindowStatus) {
        if (!canMoveTo(next)) {
            error("tariff window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): TariffWindow {
        return TariffWindow(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            shiftCode = shiftCode,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tariff window in status proposed. */
fun newTariffWindow(code: String, label: String): TariffWindow {
    return TariffWindow(
        code = code,
        label = label,
        etaMinutes = 0,
        shiftCode = "",
        country = "",
        status = TariffWindowStatus.PROPOSED,
    )
}
