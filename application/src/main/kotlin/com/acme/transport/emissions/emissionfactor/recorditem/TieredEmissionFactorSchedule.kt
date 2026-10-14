package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered emission factor schedule is in its life cycle. */
enum class TieredEmissionFactorScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tiered emission factor schedule. Its code is unique within a tenant. */
data class TieredEmissionFactorSchedule(
    var code: String,
    var title: String,
    var unit: String,
    var weight: Double,
    var name: String,
    var shiftCode: String,
    var externalRef: String,
    var segment: String,
    var contactEmail: String,
    var distanceKm: Double,
    var budget: Double,
    var status: TieredEmissionFactorScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredEmissionFactorScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredEmissionFactorScheduleStatus): Boolean {
        when (status) {
            TieredEmissionFactorScheduleStatus.NEW -> return next == TieredEmissionFactorScheduleStatus.QUEUED || next == TieredEmissionFactorScheduleStatus.FAILED
            TieredEmissionFactorScheduleStatus.QUEUED -> {
                when (next) {
                    TieredEmissionFactorScheduleStatus.DONE -> return true
                }
                return false
            }
            TieredEmissionFactorScheduleStatus.RUNNING -> {
                when (next) {
                    TieredEmissionFactorScheduleStatus.DONE, TieredEmissionFactorScheduleStatus.FAILED -> return true
                }
                return false
            }
            TieredEmissionFactorScheduleStatus.DONE -> {
                when (next) {
                    TieredEmissionFactorScheduleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredEmissionFactorScheduleStatus) {
        if (!canMoveTo(next)) {
            error("tiered emission factor schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedVolume(): Double = round(weight * budget * 100) / 100

    fun snapshot(): TieredEmissionFactorSchedule {
        return TieredEmissionFactorSchedule(
            code = code,
            title = title,
            unit = unit,
            weight = weight,
            name = name,
            shiftCode = shiftCode,
            externalRef = externalRef,
            segment = segment,
            contactEmail = contactEmail,
            distanceKm = distanceKm,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tiered emission factor schedule in status new. */
fun newTieredEmissionFactorSchedule(code: String, title: String): TieredEmissionFactorSchedule {
    return TieredEmissionFactorSchedule(
        code = code,
        title = title,
        unit = "",
        weight = 0.0,
        name = "",
        shiftCode = "",
        externalRef = "",
        segment = "",
        contactEmail = "",
        distanceKm = 0.0,
        budget = 0.0,
        status = TieredEmissionFactorScheduleStatus.NEW,
    )
}
