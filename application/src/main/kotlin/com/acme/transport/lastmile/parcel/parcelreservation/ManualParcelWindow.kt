package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual parcel window is in its life cycle. */
enum class ManualParcelWindowStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual parcel window as the parcel team keeps it. */
data class ManualParcelWindow(
    var code: String,
    var name: String,
    var source: String,
    var amount: Double,
    var channel: String,
    var updatedAt: Int,
    var createdAt: Int,
    var approved: Boolean,
    var windowDays: Int,
    var locale: String,
    var shiftCode: String,
    var status: ManualParcelWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualParcelWindowStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualParcelWindowStatus): Boolean {
        when (status) {
            ManualParcelWindowStatus.IDLE -> {
                when (next) {
                    ManualParcelWindowStatus.WARMING, ManualParcelWindowStatus.STOPPED -> return true
                }
                return false
            }
            ManualParcelWindowStatus.WARMING -> {
                when (next) {
                    ManualParcelWindowStatus.DRAINING -> return true
                }
                return false
            }
            ManualParcelWindowStatus.READY -> {
                when (next) {
                    ManualParcelWindowStatus.DRAINING -> return true
                }
                return false
            }
            ManualParcelWindowStatus.DRAINING -> return next == ManualParcelWindowStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualParcelWindowStatus) {
        if (!canMoveTo(next)) {
            error("manual parcel window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"manual parcel window"} $code"

    fun weightedExposure(): Double = round(createdAt.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): ManualParcelWindow {
        return ManualParcelWindow(
            code = code,
            name = name,
            source = source,
            amount = amount,
            channel = channel,
            updatedAt = updatedAt,
            createdAt = createdAt,
            approved = approved,
            windowDays = windowDays,
            locale = locale,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a manual parcel window in status idle. */
fun newManualParcelWindow(code: String, name: String): ManualParcelWindow {
    return ManualParcelWindow(
        code = code,
        name = name,
        source = "",
        amount = 0.0,
        channel = "",
        updatedAt = 0,
        createdAt = 0,
        approved = false,
        windowDays = 0,
        locale = "",
        shiftCode = "",
        status = ManualParcelWindowStatus.IDLE,
    )
}
