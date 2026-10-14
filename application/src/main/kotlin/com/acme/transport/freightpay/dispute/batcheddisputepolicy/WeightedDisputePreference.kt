package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted dispute preference is in its life cycle. */
enum class WeightedDisputePreferenceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One weighted dispute preference, with what the planners need to act on it. */
data class WeightedDisputePreference(
    var code: String,
    var label: String,
    var margin: Double,
    var category: String,
    var floorValue: Double,
    var weight: Double,
    var name: String,
    var status: WeightedDisputePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDisputePreferenceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDisputePreferenceStatus): Boolean {
        when (status) {
            WeightedDisputePreferenceStatus.CREATED -> return next == WeightedDisputePreferenceStatus.VALIDATED
            WeightedDisputePreferenceStatus.VALIDATED -> {
                when (next) {
                    WeightedDisputePreferenceStatus.RETIRED -> return true
                }
                return false
            }
            WeightedDisputePreferenceStatus.PUBLISHED -> return next == WeightedDisputePreferenceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedDisputePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("weighted dispute preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedTotal(): Double = round((floorValue - margin) * 100) / 100

    fun snapshot(): WeightedDisputePreference {
        return WeightedDisputePreference(
            code = code,
            label = label,
            margin = margin,
            category = category,
            floorValue = floorValue,
            weight = weight,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted dispute preference in status created. */
fun newWeightedDisputePreference(code: String, label: String): WeightedDisputePreference {
    return WeightedDisputePreference(
        code = code,
        label = label,
        margin = 0.0,
        category = "",
        floorValue = 0.0,
        weight = 0.0,
        name = "",
        status = WeightedDisputePreferenceStatus.CREATED,
    )
}
