package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.text.normalize

/** Where a weighted inspection baseline is in its life cycle. */
enum class WeightedInspectionBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted inspection baseline. Its code is unique within a tenant. */
data class WeightedInspectionBaseline(
    var code: String,
    var label: String,
    var reviewer: String,
    var loadKg: Double,
    var checksum: String,
    var locale: String,
    var status: WeightedInspectionBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedInspectionBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedInspectionBaselineStatus): Boolean {
        when (status) {
            WeightedInspectionBaselineStatus.PLANNED -> return next == WeightedInspectionBaselineStatus.COMPLETED
            WeightedInspectionBaselineStatus.SCHEDULED -> return next == WeightedInspectionBaselineStatus.IN_PROGRESS || next == WeightedInspectionBaselineStatus.CANCELLED
            WeightedInspectionBaselineStatus.IN_PROGRESS -> return next == WeightedInspectionBaselineStatus.COMPLETED || next == WeightedInspectionBaselineStatus.CANCELLED
            WeightedInspectionBaselineStatus.COMPLETED -> {
                when (next) {
                    WeightedInspectionBaselineStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedInspectionBaselineStatus) {
        if (!canMoveTo(next)) {
            error("weighted inspection baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = loadKg

    fun snapshot(): WeightedInspectionBaseline {
        return WeightedInspectionBaseline(
            code = code,
            label = label,
            reviewer = reviewer,
            loadKg = loadKg,
            checksum = checksum,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted inspection baseline in status planned. */
fun newWeightedInspectionBaseline(code: String, label: String): WeightedInspectionBaseline {
    return WeightedInspectionBaseline(
        code = code,
        label = label,
        reviewer = "",
        loadKg = 0.0,
        checksum = "",
        locale = "",
        status = WeightedInspectionBaselineStatus.PLANNED,
    )
}
