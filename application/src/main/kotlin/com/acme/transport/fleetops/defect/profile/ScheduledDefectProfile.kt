package com.acme.transport.fleetops.defect.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled defect profile records of fleetops's defect work, and the rules, stores, and calculations
// around them.

/** Where a scheduled defect profile is in its life cycle. */
enum class ScheduledDefectProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled defect profile. Its code is unique within a tenant. */
data class ScheduledDefectProfile(
    var code: String,
    var name: String,
    var loadKg: Double,
    var externalRef: String,
    var rate: Double,
    var distanceKm: Double,
    var unit: String,
    var driverId: String,
    var odometer: Int,
    var status: ScheduledDefectProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDefectProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDefectProfileStatus): Boolean {
        when (status) {
            ScheduledDefectProfileStatus.NEW -> {
                when (next) {
                    ScheduledDefectProfileStatus.RUNNING -> return true
                }
                return false
            }
            ScheduledDefectProfileStatus.QUEUED -> {
                when (next) {
                    ScheduledDefectProfileStatus.RUNNING, ScheduledDefectProfileStatus.FAILED -> return true
                }
                return false
            }
            ScheduledDefectProfileStatus.RUNNING -> return next == ScheduledDefectProfileStatus.FAILED
            ScheduledDefectProfileStatus.DONE -> return next == ScheduledDefectProfileStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledDefectProfileStatus) {
        if (!canMoveTo(next)) {
            error("scheduled defect profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled defect profile"} $code"

    fun adjustedTotal(): Double = round((distanceKm - odometer.toDouble()) * 100) / 100

    fun snapshot(): ScheduledDefectProfile {
        return ScheduledDefectProfile(
            code = code,
            name = name,
            loadKg = loadKg,
            externalRef = externalRef,
            rate = rate,
            distanceKm = distanceKm,
            unit = unit,
            driverId = driverId,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled defect profile in status new. */
fun newScheduledDefectProfile(code: String, name: String): ScheduledDefectProfile {
    return ScheduledDefectProfile(
        code = code,
        name = name,
        loadKg = 0.0,
        externalRef = "",
        rate = 0.0,
        distanceKm = 0.0,
        unit = "",
        driverId = "",
        odometer = 0,
        status = ScheduledDefectProfileStatus.NEW,
    )
}
