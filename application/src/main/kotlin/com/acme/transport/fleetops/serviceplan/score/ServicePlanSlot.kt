package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.kit.text.normalize

/** Where a service plan slot is in its life cycle. */
enum class ServicePlanSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One service plan slot, with what the planners need to act on it. */
data class ServicePlanSlot(
    var code: String,
    var label: String,
    var shiftCode: String,
    var country: String,
    var amount: Double,
    var pinned: Boolean,
    var status: ServicePlanSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanSlotStatus): Boolean {
        when (status) {
            ServicePlanSlotStatus.NEW -> {
                when (next) {
                    ServicePlanSlotStatus.DONE -> return true
                }
                return false
            }
            ServicePlanSlotStatus.QUEUED -> return next == ServicePlanSlotStatus.RUNNING
            ServicePlanSlotStatus.RUNNING -> {
                when (next) {
                    ServicePlanSlotStatus.FAILED -> return true
                }
                return false
            }
            ServicePlanSlotStatus.DONE -> return next == ServicePlanSlotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ServicePlanSlotStatus) {
        if (!canMoveTo(next)) {
            error("service plan slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = amount

    fun snapshot(): ServicePlanSlot {
        return ServicePlanSlot(
            code = code,
            label = label,
            shiftCode = shiftCode,
            country = country,
            amount = amount,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a service plan slot in status new. */
fun newServicePlanSlot(code: String, label: String): ServicePlanSlot {
    return ServicePlanSlot(
        code = code,
        label = label,
        shiftCode = "",
        country = "",
        amount = 0.0,
        pinned = false,
        status = ServicePlanSlotStatus.NEW,
    )
}
