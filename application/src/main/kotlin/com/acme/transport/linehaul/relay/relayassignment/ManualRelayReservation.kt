package com.acme.transport.linehaul.relay.relayassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual relay reservation is in its life cycle. */
enum class ManualRelayReservationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A manual relay reservation. Its code is unique within a tenant. */
data class ManualRelayReservation(
    var code: String,
    var title: String,
    var visible: Boolean,
    var pallets: Int,
    var driverId: String,
    var shiftCode: String,
    var stopCount: Int,
    var odometer: Int,
    var timezone: String,
    var status: ManualRelayReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRelayReservationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRelayReservationStatus): Boolean {
        when (status) {
            ManualRelayReservationStatus.NEW -> return next == ManualRelayReservationStatus.DONE
            ManualRelayReservationStatus.QUEUED -> {
                when (next) {
                    ManualRelayReservationStatus.DONE -> return true
                }
                return false
            }
            ManualRelayReservationStatus.RUNNING -> {
                when (next) {
                    ManualRelayReservationStatus.DONE, ManualRelayReservationStatus.FAILED -> return true
                }
                return false
            }
            ManualRelayReservationStatus.DONE -> {
                when (next) {
                    ManualRelayReservationStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualRelayReservationStatus) {
        if (!canMoveTo(next)) {
            error("manual relay reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveTotal(): Double = round((odometer.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): ManualRelayReservation {
        return ManualRelayReservation(
            code = code,
            title = title,
            visible = visible,
            pallets = pallets,
            driverId = driverId,
            shiftCode = shiftCode,
            stopCount = stopCount,
            odometer = odometer,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual relay reservation in status new. */
fun newManualRelayReservation(code: String, title: String): ManualRelayReservation {
    return ManualRelayReservation(
        code = code,
        title = title,
        visible = false,
        pallets = 0,
        driverId = "",
        shiftCode = "",
        stopCount = 0,
        odometer = 0,
        timezone = "",
        status = ManualRelayReservationStatus.NEW,
    )
}
