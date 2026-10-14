package com.acme.transport.network.servicearea.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area slot is in its life cycle. */
enum class ServiceAreaSlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A service area slot as the service area team keeps it. */
data class ServiceAreaSlot(
    var code: String,
    var label: String,
    var pallets: Int,
    var maximum: Double,
    var notes: String,
    var status: ServiceAreaSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaSlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaSlotStatus): Boolean {
        when (status) {
            ServiceAreaSlotStatus.REQUESTED -> {
                when (next) {
                    ServiceAreaSlotStatus.GRANTED, ServiceAreaSlotStatus.REVOKED -> return true
                }
                return false
            }
            ServiceAreaSlotStatus.GRANTED -> {
                when (next) {
                    ServiceAreaSlotStatus.REVOKED, ServiceAreaSlotStatus.EXPIRED -> return true
                }
                return false
            }
            ServiceAreaSlotStatus.REVOKED -> {
                when (next) {
                    ServiceAreaSlotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaSlotStatus) {
        if (!canMoveTo(next)) {
            error("service area slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedValue(): Double = round((pallets.toDouble() + maximum) * 100) / 100

    fun snapshot(): ServiceAreaSlot {
        return ServiceAreaSlot(
            code = code,
            label = label,
            pallets = pallets,
            maximum = maximum,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a service area slot in status requested. */
fun newServiceAreaSlot(code: String, label: String): ServiceAreaSlot {
    return ServiceAreaSlot(
        code = code,
        label = label,
        pallets = 0,
        maximum = 0.0,
        notes = "",
        status = ServiceAreaSlotStatus.REQUESTED,
    )
}
