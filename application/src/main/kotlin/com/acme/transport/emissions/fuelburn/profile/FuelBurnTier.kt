package com.acme.transport.emissions.fuelburn.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn tier is in its life cycle. */
enum class FuelBurnTierStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One fuel burn tier, with what the planners need to act on it. */
data class FuelBurnTier(
    var code: String,
    var label: String,
    var country: String,
    var visible: Boolean,
    var archived: Boolean,
    var weight: Double,
    var ceiling: Double,
    var revision: Int,
    var status: FuelBurnTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnTierStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnTierStatus): Boolean {
        when (status) {
            FuelBurnTierStatus.CREATED -> return next == FuelBurnTierStatus.RETIRED
            FuelBurnTierStatus.VALIDATED -> return next == FuelBurnTierStatus.RETIRED
            FuelBurnTierStatus.PUBLISHED -> return next == FuelBurnTierStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FuelBurnTierStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn tier $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedValue(): Double = round(weight * ceiling * 100) / 100

    fun snapshot(): FuelBurnTier {
        return FuelBurnTier(
            code = code,
            label = label,
            country = country,
            visible = visible,
            archived = archived,
            weight = weight,
            ceiling = ceiling,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a fuel burn tier in status created. */
fun newFuelBurnTier(code: String, label: String): FuelBurnTier {
    return FuelBurnTier(
        code = code,
        label = label,
        country = "",
        visible = false,
        archived = false,
        weight = 0.0,
        ceiling = 0.0,
        revision = 0,
        status = FuelBurnTierStatus.CREATED,
    )
}
