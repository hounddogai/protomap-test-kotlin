package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual fuel surcharge schedule is in its life cycle. */
enum class ManualFuelSurchargeScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual fuel surcharge schedule as the fuel surcharge team keeps it. */
data class ManualFuelSurchargeSchedule(
    var code: String,
    var name: String,
    var floorValue: Double,
    var stopCount: Int,
    var capacity: Int,
    var ratio: Double,
    var region: String,
    var status: ManualFuelSurchargeScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualFuelSurchargeScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualFuelSurchargeScheduleStatus): Boolean {
        when (status) {
            ManualFuelSurchargeScheduleStatus.DRAFT -> {
                when (next) {
                    ManualFuelSurchargeScheduleStatus.PENDING -> return true
                }
                return false
            }
            ManualFuelSurchargeScheduleStatus.PENDING -> return next == ManualFuelSurchargeScheduleStatus.SUSPENDED || next == ManualFuelSurchargeScheduleStatus.CLOSED
            ManualFuelSurchargeScheduleStatus.ACTIVE -> {
                when (next) {
                    ManualFuelSurchargeScheduleStatus.CLOSED -> return true
                }
                return false
            }
            ManualFuelSurchargeScheduleStatus.SUSPENDED -> return next == ManualFuelSurchargeScheduleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ManualFuelSurchargeScheduleStatus) {
        if (!canMoveTo(next)) {
            error("manual fuel surcharge schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round((floorValue - capacity.toDouble()) * 100) / 100

    fun snapshot(): ManualFuelSurchargeSchedule {
        return ManualFuelSurchargeSchedule(
            code = code,
            name = name,
            floorValue = floorValue,
            stopCount = stopCount,
            capacity = capacity,
            ratio = ratio,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a manual fuel surcharge schedule in status draft. */
fun newManualFuelSurchargeSchedule(code: String, name: String): ManualFuelSurchargeSchedule {
    return ManualFuelSurchargeSchedule(
        code = code,
        name = name,
        floorValue = 0.0,
        stopCount = 0,
        capacity = 0,
        ratio = 0.0,
        region = "",
        status = ManualFuelSurchargeScheduleStatus.DRAFT,
    )
}
