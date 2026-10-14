package com.acme.transport.emissions.idling.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal idling claim is in its life cycle. */
enum class SeasonalIdlingClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a seasonal idling claim and its current status. */
data class SeasonalIdlingClaim(
    var code: String,
    var label: String,
    var title: String,
    var shiftCode: String,
    var threshold: Double,
    var country: String,
    var fuelLiters: Double,
    var dock: String,
    var status: SeasonalIdlingClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalIdlingClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalIdlingClaimStatus): Boolean {
        when (status) {
            SeasonalIdlingClaimStatus.CREATED -> {
                when (next) {
                    SeasonalIdlingClaimStatus.VALIDATED, SeasonalIdlingClaimStatus.RETIRED -> return true
                }
                return false
            }
            SeasonalIdlingClaimStatus.VALIDATED -> {
                when (next) {
                    SeasonalIdlingClaimStatus.PUBLISHED -> return true
                }
                return false
            }
            SeasonalIdlingClaimStatus.PUBLISHED -> {
                when (next) {
                    SeasonalIdlingClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalIdlingClaimStatus) {
        if (!canMoveTo(next)) {
            error("seasonal idling claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedLoad(): Double = round((threshold + fuelLiters) * 100) / 100

    fun snapshot(): SeasonalIdlingClaim {
        return SeasonalIdlingClaim(
            code = code,
            label = label,
            title = title,
            shiftCode = shiftCode,
            threshold = threshold,
            country = country,
            fuelLiters = fuelLiters,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a seasonal idling claim in status created. */
fun newSeasonalIdlingClaim(code: String, label: String): SeasonalIdlingClaim {
    return SeasonalIdlingClaim(
        code = code,
        label = label,
        title = "",
        shiftCode = "",
        threshold = 0.0,
        country = "",
        fuelLiters = 0.0,
        dock = "",
        status = SeasonalIdlingClaimStatus.CREATED,
    )
}
