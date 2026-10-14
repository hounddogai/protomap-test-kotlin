package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default service area rollup is in its life cycle. */
enum class DefaultServiceAreaRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default service area rollup as the service area team keeps it. */
data class DefaultServiceAreaRollup(
    var code: String,
    var name: String,
    var vehicleId: String,
    var shiftCode: String,
    var durationMinutes: Int,
    var distanceKm: Double,
    var contactEmail: String,
    var score: Double,
    var fuelLiters: Double,
    var status: DefaultServiceAreaRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultServiceAreaRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultServiceAreaRollupStatus): Boolean {
        when (status) {
            DefaultServiceAreaRollupStatus.CREATED -> {
                when (next) {
                    DefaultServiceAreaRollupStatus.PUBLISHED -> return true
                }
                return false
            }
            DefaultServiceAreaRollupStatus.VALIDATED -> {
                when (next) {
                    DefaultServiceAreaRollupStatus.PUBLISHED, DefaultServiceAreaRollupStatus.RETIRED -> return true
                }
                return false
            }
            DefaultServiceAreaRollupStatus.PUBLISHED -> {
                when (next) {
                    DefaultServiceAreaRollupStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultServiceAreaRollupStatus) {
        if (!canMoveTo(next)) {
            error("default service area rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveVolume(): Double = round((score + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): DefaultServiceAreaRollup {
        return DefaultServiceAreaRollup(
            code = code,
            name = name,
            vehicleId = vehicleId,
            shiftCode = shiftCode,
            durationMinutes = durationMinutes,
            distanceKm = distanceKm,
            contactEmail = contactEmail,
            score = score,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default service area rollup in status created. */
fun newDefaultServiceAreaRollup(code: String, name: String): DefaultServiceAreaRollup {
    return DefaultServiceAreaRollup(
        code = code,
        name = name,
        vehicleId = "",
        shiftCode = "",
        durationMinutes = 0,
        distanceKm = 0.0,
        contactEmail = "",
        score = 0.0,
        fuelLiters = 0.0,
        status = DefaultServiceAreaRollupStatus.CREATED,
    )
}
