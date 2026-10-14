package com.acme.transport.network.node.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal node agreement is in its life cycle. */
enum class SeasonalNodeAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a seasonal node agreement and its current status. */
data class SeasonalNodeAgreement(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var driverId: String,
    var distanceKm: Double,
    var status: SeasonalNodeAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalNodeAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalNodeAgreementStatus): Boolean {
        when (status) {
            SeasonalNodeAgreementStatus.CREATED -> {
                when (next) {
                    SeasonalNodeAgreementStatus.VALIDATED -> return true
                }
                return false
            }
            SeasonalNodeAgreementStatus.VALIDATED -> return next == SeasonalNodeAgreementStatus.PUBLISHED || next == SeasonalNodeAgreementStatus.RETIRED
            SeasonalNodeAgreementStatus.PUBLISHED -> {
                when (next) {
                    SeasonalNodeAgreementStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalNodeAgreementStatus) {
        if (!canMoveTo(next)) {
            error("seasonal node agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal node agreement"} $code"

    fun adjustedLoad(): Double = round((distanceKm - fuelLiters) * 100) / 100

    fun snapshot(): SeasonalNodeAgreement {
        return SeasonalNodeAgreement(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            driverId = driverId,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal node agreement in status created. */
fun newSeasonalNodeAgreement(code: String, label: String): SeasonalNodeAgreement {
    return SeasonalNodeAgreement(
        code = code,
        label = label,
        fuelLiters = 0.0,
        driverId = "",
        distanceKm = 0.0,
        status = SeasonalNodeAgreementStatus.CREATED,
    )
}
