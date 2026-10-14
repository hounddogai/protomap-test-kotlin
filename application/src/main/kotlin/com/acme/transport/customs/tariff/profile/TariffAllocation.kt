package com.acme.transport.customs.tariff.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff allocation is in its life cycle. */
enum class TariffAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tariff allocation as the tariff team keeps it. */
data class TariffAllocation(
    var code: String,
    var title: String,
    var routeCode: String,
    var externalRef: String,
    var durationMinutes: Int,
    var vehicleId: String,
    var amount: Double,
    var ratio: Double,
    var batchSize: Int,
    var graceDays: Int,
    var margin: Double,
    var status: TariffAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffAllocationStatus): Boolean {
        when (status) {
            TariffAllocationStatus.PLANNED -> {
                when (next) {
                    TariffAllocationStatus.SCHEDULED, TariffAllocationStatus.IN_PROGRESS -> return true
                }
                return false
            }
            TariffAllocationStatus.SCHEDULED -> return next == TariffAllocationStatus.IN_PROGRESS || next == TariffAllocationStatus.COMPLETED
            TariffAllocationStatus.IN_PROGRESS -> {
                when (next) {
                    TariffAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            TariffAllocationStatus.COMPLETED -> return next == TariffAllocationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TariffAllocationStatus) {
        if (!canMoveTo(next)) {
            error("tariff allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedLoad(): Double = round(durationMinutes.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): TariffAllocation {
        return TariffAllocation(
            code = code,
            title = title,
            routeCode = routeCode,
            externalRef = externalRef,
            durationMinutes = durationMinutes,
            vehicleId = vehicleId,
            amount = amount,
            ratio = ratio,
            batchSize = batchSize,
            graceDays = graceDays,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tariff allocation in status planned. */
fun newTariffAllocation(code: String, title: String): TariffAllocation {
    return TariffAllocation(
        code = code,
        title = title,
        routeCode = "",
        externalRef = "",
        durationMinutes = 0,
        vehicleId = "",
        amount = 0.0,
        ratio = 0.0,
        batchSize = 0,
        graceDays = 0,
        margin = 0.0,
        status = TariffAllocationStatus.PLANNED,
    )
}
