package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal fuel surcharge preference is in its life cycle. */
enum class SeasonalFuelSurchargePreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal fuel surcharge preference as the fuel surcharge team keeps it. */
data class SeasonalFuelSurchargePreference(
    var code: String,
    var label: String,
    var score: Double,
    var ratio: Double,
    var budget: Double,
    var minimum: Double,
    var title: String,
    var approved: Boolean,
    var status: SeasonalFuelSurchargePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalFuelSurchargePreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalFuelSurchargePreferenceStatus): Boolean {
        when (status) {
            SeasonalFuelSurchargePreferenceStatus.REQUESTED -> {
                when (next) {
                    SeasonalFuelSurchargePreferenceStatus.REVOKED -> return true
                }
                return false
            }
            SeasonalFuelSurchargePreferenceStatus.GRANTED -> {
                when (next) {
                    SeasonalFuelSurchargePreferenceStatus.REVOKED -> return true
                }
                return false
            }
            SeasonalFuelSurchargePreferenceStatus.REVOKED -> return next == SeasonalFuelSurchargePreferenceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalFuelSurchargePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("seasonal fuel surcharge preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round((score + minimum) * 100) / 100

    fun snapshot(): SeasonalFuelSurchargePreference {
        return SeasonalFuelSurchargePreference(
            code = code,
            label = label,
            score = score,
            ratio = ratio,
            budget = budget,
            minimum = minimum,
            title = title,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal fuel surcharge preference in status requested. */
fun newSeasonalFuelSurchargePreference(code: String, label: String): SeasonalFuelSurchargePreference {
    return SeasonalFuelSurchargePreference(
        code = code,
        label = label,
        score = 0.0,
        ratio = 0.0,
        budget = 0.0,
        minimum = 0.0,
        title = "",
        approved = false,
        status = SeasonalFuelSurchargePreferenceStatus.REQUESTED,
    )
}
