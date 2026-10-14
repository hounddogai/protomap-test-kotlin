package com.acme.transport.lastmile.locker.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The locker window records of lastmile's locker work, and the rules, stores, and calculations around them.

/** Where a locker window is in its life cycle. */
enum class LockerWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a locker window and its current status. */
data class LockerWindow(
    var code: String,
    var name: String,
    var floorValue: Double,
    var driverId: String,
    var durationMinutes: Int,
    var category: String,
    var amount: Double,
    var status: LockerWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerWindowStatus): Boolean {
        when (status) {
            LockerWindowStatus.DRAFT -> return next == LockerWindowStatus.ACTIVE || next == LockerWindowStatus.CLOSED
            LockerWindowStatus.PENDING -> {
                when (next) {
                    LockerWindowStatus.SUSPENDED -> return true
                }
                return false
            }
            LockerWindowStatus.ACTIVE -> {
                when (next) {
                    LockerWindowStatus.SUSPENDED -> return true
                }
                return false
            }
            LockerWindowStatus.SUSPENDED -> {
                when (next) {
                    LockerWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerWindowStatus) {
        if (!canMoveTo(next)) {
            error("locker window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"locker window"} $code"

    fun netLoad(): Double = round((amount + floorValue) * 100) / 100

    fun snapshot(): LockerWindow {
        return LockerWindow(
            code = code,
            name = name,
            floorValue = floorValue,
            driverId = driverId,
            durationMinutes = durationMinutes,
            category = category,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a locker window in status draft. */
fun newLockerWindow(code: String, name: String): LockerWindow {
    return LockerWindow(
        code = code,
        name = name,
        floorValue = 0.0,
        driverId = "",
        durationMinutes = 0,
        category = "",
        amount = 0.0,
        status = LockerWindowStatus.DRAFT,
    )
}
