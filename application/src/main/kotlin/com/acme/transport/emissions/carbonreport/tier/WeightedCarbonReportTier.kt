package com.acme.transport.emissions.carbonreport.tier

import com.acme.transport.kit.text.normalize

// The weighted carbon report tier records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a weighted carbon report tier is in its life cycle. */
enum class WeightedCarbonReportTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted carbon report tier. Its code is unique within a tenant. */
data class WeightedCarbonReportTier(
    var code: String,
    var title: String,
    var active: Boolean,
    var description: String,
    var notes: String,
    var label: String,
    var quantity: Int,
    var status: WeightedCarbonReportTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCarbonReportTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCarbonReportTierStatus): Boolean {
        when (status) {
            WeightedCarbonReportTierStatus.REQUESTED -> {
                when (next) {
                    WeightedCarbonReportTierStatus.REVOKED, WeightedCarbonReportTierStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedCarbonReportTierStatus.GRANTED -> {
                when (next) {
                    WeightedCarbonReportTierStatus.REVOKED, WeightedCarbonReportTierStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedCarbonReportTierStatus.REVOKED -> {
                when (next) {
                    WeightedCarbonReportTierStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedCarbonReportTierStatus) {
        if (!canMoveTo(next)) {
            error("weighted carbon report tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): WeightedCarbonReportTier {
        return WeightedCarbonReportTier(
            code = code,
            title = title,
            active = active,
            description = description,
            notes = notes,
            label = label,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted carbon report tier in status requested. */
fun newWeightedCarbonReportTier(code: String, title: String): WeightedCarbonReportTier {
    return WeightedCarbonReportTier(
        code = code,
        title = title,
        active = false,
        description = "",
        notes = "",
        label = "",
        quantity = 0,
        status = WeightedCarbonReportTierStatus.REQUESTED,
    )
}
