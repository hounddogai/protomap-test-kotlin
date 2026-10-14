package com.acme.transport.customs.declaration.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration record is in its life cycle. */
enum class DeclarationRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One declaration record, with what the planners need to act on it. */
data class DeclarationRecord(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var vehicleId: String,
    var discount: Double,
    var windowDays: Int,
    var retries: Int,
    var status: DeclarationRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationRecordStatus): Boolean {
        when (status) {
            DeclarationRecordStatus.IDLE -> {
                when (next) {
                    DeclarationRecordStatus.WARMING, DeclarationRecordStatus.READY -> return true
                }
                return false
            }
            DeclarationRecordStatus.WARMING -> return next == DeclarationRecordStatus.DRAINING
            DeclarationRecordStatus.READY -> {
                when (next) {
                    DeclarationRecordStatus.DRAINING, DeclarationRecordStatus.STOPPED -> return true
                }
                return false
            }
            DeclarationRecordStatus.DRAINING -> {
                when (next) {
                    DeclarationRecordStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationRecordStatus) {
        if (!canMoveTo(next)) {
            error("declaration record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedExposure(): Double = round((discount - windowDays.toDouble()) * 100) / 100

    fun snapshot(): DeclarationRecord {
        return DeclarationRecord(
            code = code,
            label = label,
            distanceKm = distanceKm,
            vehicleId = vehicleId,
            discount = discount,
            windowDays = windowDays,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a declaration record in status idle. */
fun newDeclarationRecord(code: String, label: String): DeclarationRecord {
    return DeclarationRecord(
        code = code,
        label = label,
        distanceKm = 0.0,
        vehicleId = "",
        discount = 0.0,
        windowDays = 0,
        retries = 0,
        status = DeclarationRecordStatus.IDLE,
    )
}
