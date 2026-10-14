package com.acme.transport.emissions.fuelburn.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling fuel burn record is in its life cycle. */
enum class RollingFuelBurnRecordStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rolling fuel burn record as the fuel burn team keeps it. */
data class RollingFuelBurnRecord(
    var code: String,
    var name: String,
    var notes: String,
    var trailerId: String,
    var odometer: Int,
    var contactEmail: String,
    var loadKg: Double,
    var carrier: String,
    var threshold: Double,
    var status: RollingFuelBurnRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFuelBurnRecordStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFuelBurnRecordStatus): Boolean {
        when (status) {
            RollingFuelBurnRecordStatus.OPEN -> {
                when (next) {
                    RollingFuelBurnRecordStatus.REOPENED -> return true
                }
                return false
            }
            RollingFuelBurnRecordStatus.IN_REVIEW -> {
                when (next) {
                    RollingFuelBurnRecordStatus.REOPENED, RollingFuelBurnRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingFuelBurnRecordStatus.RESOLVED -> return next == RollingFuelBurnRecordStatus.ARCHIVED
            RollingFuelBurnRecordStatus.REOPENED -> {
                when (next) {
                    RollingFuelBurnRecordStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingFuelBurnRecordStatus) {
        if (!canMoveTo(next)) {
            error("rolling fuel burn record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling fuel burn record"} $code"

    fun adjustedTotal(): Double = round(odometer.toDouble() * loadKg * 100) / 100

    fun snapshot(): RollingFuelBurnRecord {
        return RollingFuelBurnRecord(
            code = code,
            name = name,
            notes = notes,
            trailerId = trailerId,
            odometer = odometer,
            contactEmail = contactEmail,
            loadKg = loadKg,
            carrier = carrier,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling fuel burn record in status open. */
fun newRollingFuelBurnRecord(code: String, name: String): RollingFuelBurnRecord {
    return RollingFuelBurnRecord(
        code = code,
        name = name,
        notes = "",
        trailerId = "",
        odometer = 0,
        contactEmail = "",
        loadKg = 0.0,
        carrier = "",
        threshold = 0.0,
        status = RollingFuelBurnRecordStatus.OPEN,
    )
}
