package com.acme.transport.freightpay.fuelsurcharge.group

import com.acme.transport.kit.text.normalize

/** Where a fuel surcharge schedule is in its life cycle. */
enum class FuelSurchargeScheduleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fuel surcharge schedule. Its code is unique within a tenant. */
data class FuelSurchargeSchedule(
    var code: String,
    var name: String,
    var carrier: String,
    var locale: String,
    var ceiling: Double,
    var status: FuelSurchargeScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeScheduleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeScheduleStatus): Boolean {
        when (status) {
            FuelSurchargeScheduleStatus.PLANNED -> return next == FuelSurchargeScheduleStatus.SCHEDULED
            FuelSurchargeScheduleStatus.SCHEDULED -> return next == FuelSurchargeScheduleStatus.IN_PROGRESS || next == FuelSurchargeScheduleStatus.COMPLETED
            FuelSurchargeScheduleStatus.IN_PROGRESS -> {
                when (next) {
                    FuelSurchargeScheduleStatus.COMPLETED, FuelSurchargeScheduleStatus.CANCELLED -> return true
                }
                return false
            }
            FuelSurchargeScheduleStatus.COMPLETED -> return next == FuelSurchargeScheduleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeScheduleStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fuel surcharge schedule"} $code"

    fun measuredValue(): Double = ceiling

    fun snapshot(): FuelSurchargeSchedule {
        return FuelSurchargeSchedule(
            code = code,
            name = name,
            carrier = carrier,
            locale = locale,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fuel surcharge schedule in status planned. */
fun newFuelSurchargeSchedule(code: String, name: String): FuelSurchargeSchedule {
    return FuelSurchargeSchedule(
        code = code,
        name = name,
        carrier = "",
        locale = "",
        ceiling = 0.0,
        status = FuelSurchargeScheduleStatus.PLANNED,
    )
}
