package com.acme.transport.linehaul.tender.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender checkpoint is in its life cycle. */
enum class TenderCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One tender checkpoint, with what the planners need to act on it. */
data class TenderCheckpoint(
    var code: String,
    var title: String,
    var vehicleId: String,
    var floorValue: Double,
    var windowDays: Int,
    var sequence: Int,
    var currency: String,
    var label: String,
    var phone: String,
    var driverId: String,
    var fuelLiters: Double,
    var status: TenderCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderCheckpointStatus): Boolean {
        when (status) {
            TenderCheckpointStatus.DRAFT -> {
                when (next) {
                    TenderCheckpointStatus.SUSPENDED -> return true
                }
                return false
            }
            TenderCheckpointStatus.PENDING -> return next == TenderCheckpointStatus.ACTIVE || next == TenderCheckpointStatus.SUSPENDED
            TenderCheckpointStatus.ACTIVE -> return next == TenderCheckpointStatus.SUSPENDED || next == TenderCheckpointStatus.CLOSED
            TenderCheckpointStatus.SUSPENDED -> return next == TenderCheckpointStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TenderCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("tender checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun adjustedExposure(): Double = round((sequence.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): TenderCheckpoint {
        return TenderCheckpoint(
            code = code,
            title = title,
            vehicleId = vehicleId,
            floorValue = floorValue,
            windowDays = windowDays,
            sequence = sequence,
            currency = currency,
            label = label,
            phone = phone,
            driverId = driverId,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tender checkpoint in status draft. */
fun newTenderCheckpoint(code: String, title: String): TenderCheckpoint {
    return TenderCheckpoint(
        code = code,
        title = title,
        vehicleId = "",
        floorValue = 0.0,
        windowDays = 0,
        sequence = 0,
        currency = "",
        label = "",
        phone = "",
        driverId = "",
        fuelLiters = 0.0,
        status = TenderCheckpointStatus.DRAFT,
    )
}
