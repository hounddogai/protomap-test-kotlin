package com.acme.transport.customs.tariff.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback tariff allocation is in its life cycle. */
enum class FallbackTariffAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One fallback tariff allocation, with what the planners need to act on it. */
data class FallbackTariffAllocation(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var weight: Double,
    var windowDays: Int,
    var locale: String,
    var checksum: String,
    var status: FallbackTariffAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTariffAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTariffAllocationStatus): Boolean {
        when (status) {
            FallbackTariffAllocationStatus.PROPOSED -> return next == FallbackTariffAllocationStatus.WITHDRAWN
            FallbackTariffAllocationStatus.APPROVED -> {
                when (next) {
                    FallbackTariffAllocationStatus.REJECTED -> return true
                }
                return false
            }
            FallbackTariffAllocationStatus.REJECTED -> {
                when (next) {
                    FallbackTariffAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackTariffAllocationStatus) {
        if (!canMoveTo(next)) {
            error("fallback tariff allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback tariff allocation"} $code"

    fun weightedValue(): Double = round((windowDays.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): FallbackTariffAllocation {
        return FallbackTariffAllocation(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            weight = weight,
            windowDays = windowDays,
            locale = locale,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback tariff allocation in status proposed. */
fun newFallbackTariffAllocation(code: String, label: String): FallbackTariffAllocation {
    return FallbackTariffAllocation(
        code = code,
        label = label,
        fuelLiters = 0.0,
        weight = 0.0,
        windowDays = 0,
        locale = "",
        checksum = "",
        status = FallbackTariffAllocationStatus.PROPOSED,
    )
}
