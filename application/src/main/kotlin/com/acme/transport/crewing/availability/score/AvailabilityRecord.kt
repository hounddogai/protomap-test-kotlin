package com.acme.transport.crewing.availability.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability record is in its life cycle. */
enum class AvailabilityRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A availability record. Its code is unique within a tenant. */
data class AvailabilityRecord(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var summary: String,
    var minimum: Double,
    var externalRef: String,
    var vehicleId: String,
    var currency: String,
    var status: AvailabilityRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityRecordStatus): Boolean {
        when (status) {
            AvailabilityRecordStatus.REQUESTED -> {
                when (next) {
                    AvailabilityRecordStatus.EXPIRED -> return true
                }
                return false
            }
            AvailabilityRecordStatus.GRANTED -> return next == AvailabilityRecordStatus.REVOKED || next == AvailabilityRecordStatus.EXPIRED
            AvailabilityRecordStatus.REVOKED -> return next == AvailabilityRecordStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AvailabilityRecordStatus) {
        if (!canMoveTo(next)) {
            error("availability record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"availability record"} $code"

    fun weightedLoad(): Double = round((fuelLiters - minimum) * 100) / 100

    fun snapshot(): AvailabilityRecord {
        return AvailabilityRecord(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            summary = summary,
            minimum = minimum,
            externalRef = externalRef,
            vehicleId = vehicleId,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a availability record in status requested. */
fun newAvailabilityRecord(code: String, title: String): AvailabilityRecord {
    return AvailabilityRecord(
        code = code,
        title = title,
        fuelLiters = 0.0,
        summary = "",
        minimum = 0.0,
        externalRef = "",
        vehicleId = "",
        currency = "",
        status = AvailabilityRecordStatus.REQUESTED,
    )
}
