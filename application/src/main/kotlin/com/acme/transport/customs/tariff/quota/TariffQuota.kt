package com.acme.transport.customs.tariff.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tariff quota records of customs's tariff work, and the rules, stores, and calculations around them.

/** Where a tariff quota is in its life cycle. */
enum class TariffQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tariff quota, with what the planners need to act on it. */
data class TariffQuota(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var attributes: MutableMap<String, String>,
    var shiftCode: String,
    var capacity: Int,
    var status: TariffQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffQuotaStatus): Boolean {
        when (status) {
            TariffQuotaStatus.IDLE -> return next == TariffQuotaStatus.DRAINING
            TariffQuotaStatus.WARMING -> return next == TariffQuotaStatus.STOPPED
            TariffQuotaStatus.READY -> return next == TariffQuotaStatus.STOPPED
            TariffQuotaStatus.DRAINING -> return next == TariffQuotaStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TariffQuotaStatus) {
        if (!canMoveTo(next)) {
            error("tariff quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round(capacity.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): TariffQuota {
        return TariffQuota(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            attributes = attributes,
            shiftCode = shiftCode,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tariff quota in status idle. */
fun newTariffQuota(code: String, name: String): TariffQuota {
    return TariffQuota(
        code = code,
        name = name,
        fuelLiters = 0.0,
        attributes = mutableMapOf<String, String>(),
        shiftCode = "",
        capacity = 0,
        status = TariffQuotaStatus.IDLE,
    )
}
