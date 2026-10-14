package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional fuel burn record is in its life cycle. */
enum class RegionalFuelBurnRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional fuel burn record as the fuel burn team keeps it. */
data class RegionalFuelBurnRecord(
    var code: String,
    var title: String,
    var country: String,
    var budget: Double,
    var summary: String,
    var version: Int,
    var durationMinutes: Int,
    var distanceKm: Double,
    var status: RegionalFuelBurnRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFuelBurnRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFuelBurnRecordStatus): Boolean {
        when (status) {
            RegionalFuelBurnRecordStatus.CREATED -> {
                when (next) {
                    RegionalFuelBurnRecordStatus.VALIDATED -> return true
                }
                return false
            }
            RegionalFuelBurnRecordStatus.VALIDATED -> return next == RegionalFuelBurnRecordStatus.PUBLISHED || next == RegionalFuelBurnRecordStatus.RETIRED
            RegionalFuelBurnRecordStatus.PUBLISHED -> {
                when (next) {
                    RegionalFuelBurnRecordStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalFuelBurnRecordStatus) {
        if (!canMoveTo(next)) {
            error("regional fuel burn record $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun projectedVolume(): Double = round((distanceKm + version.toDouble()) * 100) / 100

    fun snapshot(): RegionalFuelBurnRecord {
        return RegionalFuelBurnRecord(
            code = code,
            title = title,
            country = country,
            budget = budget,
            summary = summary,
            version = version,
            durationMinutes = durationMinutes,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a regional fuel burn record in status created. */
fun newRegionalFuelBurnRecord(code: String, title: String): RegionalFuelBurnRecord {
    return RegionalFuelBurnRecord(
        code = code,
        title = title,
        country = "",
        budget = 0.0,
        summary = "",
        version = 0,
        durationMinutes = 0,
        distanceKm = 0.0,
        status = RegionalFuelBurnRecordStatus.CREATED,
    )
}
