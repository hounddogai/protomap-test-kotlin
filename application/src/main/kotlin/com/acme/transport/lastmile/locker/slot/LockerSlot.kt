package com.acme.transport.lastmile.locker.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The locker slot records of lastmile's locker work, and the rules, stores, and calculations around them.

/** Where a locker slot is in its life cycle. */
enum class LockerSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a locker slot and its current status. */
data class LockerSlot(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var ceiling: Double,
    var routeCode: String,
    var timezone: String,
    var locale: String,
    var quantity: Int,
    var loadKg: Double,
    var source: String,
    var status: LockerSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerSlotStatus): Boolean {
        when (status) {
            LockerSlotStatus.NEW -> return next == LockerSlotStatus.QUEUED
            LockerSlotStatus.QUEUED -> return next == LockerSlotStatus.FAILED
            LockerSlotStatus.RUNNING -> return next == LockerSlotStatus.DONE || next == LockerSlotStatus.FAILED
            LockerSlotStatus.DONE -> return next == LockerSlotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LockerSlotStatus) {
        if (!canMoveTo(next)) {
            error("locker slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"locker slot"} $code"

    fun adjustedVolume(): Double = round((loadKg + quantity.toDouble()) * 100) / 100

    fun snapshot(): LockerSlot {
        return LockerSlot(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            ceiling = ceiling,
            routeCode = routeCode,
            timezone = timezone,
            locale = locale,
            quantity = quantity,
            loadKg = loadKg,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a locker slot in status new. */
fun newLockerSlot(code: String, name: String): LockerSlot {
    return LockerSlot(
        code = code,
        name = name,
        fuelLiters = 0.0,
        ceiling = 0.0,
        routeCode = "",
        timezone = "",
        locale = "",
        quantity = 0,
        loadKg = 0.0,
        source = "",
        status = LockerSlotStatus.NEW,
    )
}
