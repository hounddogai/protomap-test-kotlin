package com.acme.transport.customs.inspection.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted inspection rollup is in its life cycle. */
enum class WeightedInspectionRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A weighted inspection rollup. Its code is unique within a tenant. */
data class WeightedInspectionRollup(
    var code: String,
    var title: String,
    var odometer: Int,
    var distanceKm: Double,
    var weight: Double,
    var ceiling: Double,
    var locale: String,
    var retries: Int,
    var shiftCode: String,
    var status: WeightedInspectionRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedInspectionRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedInspectionRollupStatus): Boolean {
        when (status) {
            WeightedInspectionRollupStatus.PROPOSED -> return next == WeightedInspectionRollupStatus.APPROVED || next == WeightedInspectionRollupStatus.WITHDRAWN
            WeightedInspectionRollupStatus.APPROVED -> return next == WeightedInspectionRollupStatus.REJECTED || next == WeightedInspectionRollupStatus.WITHDRAWN
            WeightedInspectionRollupStatus.REJECTED -> return next == WeightedInspectionRollupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: WeightedInspectionRollupStatus) {
        if (!canMoveTo(next)) {
            error("weighted inspection rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted inspection rollup"} $code"

    fun effectiveExposure(): Double = round((distanceKm - weight) * 100) / 100

    fun snapshot(): WeightedInspectionRollup {
        return WeightedInspectionRollup(
            code = code,
            title = title,
            odometer = odometer,
            distanceKm = distanceKm,
            weight = weight,
            ceiling = ceiling,
            locale = locale,
            retries = retries,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted inspection rollup in status proposed. */
fun newWeightedInspectionRollup(code: String, title: String): WeightedInspectionRollup {
    return WeightedInspectionRollup(
        code = code,
        title = title,
        odometer = 0,
        distanceKm = 0.0,
        weight = 0.0,
        ceiling = 0.0,
        locale = "",
        retries = 0,
        shiftCode = "",
        status = WeightedInspectionRollupStatus.PROPOSED,
    )
}
