package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area entry is in its life cycle. */
enum class ServiceAreaEntryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A service area entry. Its code is unique within a tenant. */
data class ServiceAreaEntry(
    var code: String,
    var name: String,
    var score: Double,
    var pallets: Int,
    var ceiling: Double,
    var budget: Double,
    var timezone: String,
    var fuelLiters: Double,
    var label: String,
    var locale: String,
    var discount: Double,
    var status: ServiceAreaEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaEntryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaEntryStatus): Boolean {
        when (status) {
            ServiceAreaEntryStatus.IDLE -> return next == ServiceAreaEntryStatus.READY
            ServiceAreaEntryStatus.WARMING -> {
                when (next) {
                    ServiceAreaEntryStatus.DRAINING -> return true
                }
                return false
            }
            ServiceAreaEntryStatus.READY -> return next == ServiceAreaEntryStatus.DRAINING || next == ServiceAreaEntryStatus.STOPPED
            ServiceAreaEntryStatus.DRAINING -> return next == ServiceAreaEntryStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ServiceAreaEntryStatus) {
        if (!canMoveTo(next)) {
            error("service area entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"service area entry"} $code"

    fun effectiveTotal(): Double = round(fuelLiters * pallets.toDouble() * 100) / 100

    fun snapshot(): ServiceAreaEntry {
        return ServiceAreaEntry(
            code = code,
            name = name,
            score = score,
            pallets = pallets,
            ceiling = ceiling,
            budget = budget,
            timezone = timezone,
            fuelLiters = fuelLiters,
            label = label,
            locale = locale,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a service area entry in status idle. */
fun newServiceAreaEntry(code: String, name: String): ServiceAreaEntry {
    return ServiceAreaEntry(
        code = code,
        name = name,
        score = 0.0,
        pallets = 0,
        ceiling = 0.0,
        budget = 0.0,
        timezone = "",
        fuelLiters = 0.0,
        label = "",
        locale = "",
        discount = 0.0,
        status = ServiceAreaEntryStatus.IDLE,
    )
}
