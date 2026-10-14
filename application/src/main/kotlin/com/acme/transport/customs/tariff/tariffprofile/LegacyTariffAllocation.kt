package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.kit.text.normalize

/** Where a legacy tariff allocation is in its life cycle. */
enum class LegacyTariffAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One legacy tariff allocation, with what the planners need to act on it. */
data class LegacyTariffAllocation(
    var code: String,
    var name: String,
    var unit: String,
    var expiresAt: Int,
    var shiftCode: String,
    var status: LegacyTariffAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTariffAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTariffAllocationStatus): Boolean {
        when (status) {
            LegacyTariffAllocationStatus.PLANNED -> return next == LegacyTariffAllocationStatus.SCHEDULED || next == LegacyTariffAllocationStatus.IN_PROGRESS
            LegacyTariffAllocationStatus.SCHEDULED -> return next == LegacyTariffAllocationStatus.CANCELLED
            LegacyTariffAllocationStatus.IN_PROGRESS -> return next == LegacyTariffAllocationStatus.COMPLETED || next == LegacyTariffAllocationStatus.CANCELLED
            LegacyTariffAllocationStatus.COMPLETED -> {
                when (next) {
                    LegacyTariffAllocationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyTariffAllocationStatus) {
        if (!canMoveTo(next)) {
            error("legacy tariff allocation $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): LegacyTariffAllocation {
        return LegacyTariffAllocation(
            code = code,
            name = name,
            unit = unit,
            expiresAt = expiresAt,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a legacy tariff allocation in status planned. */
fun newLegacyTariffAllocation(code: String, name: String): LegacyTariffAllocation {
    return LegacyTariffAllocation(
        code = code,
        name = name,
        unit = "",
        expiresAt = 0,
        shiftCode = "",
        status = LegacyTariffAllocationStatus.PLANNED,
    )
}
