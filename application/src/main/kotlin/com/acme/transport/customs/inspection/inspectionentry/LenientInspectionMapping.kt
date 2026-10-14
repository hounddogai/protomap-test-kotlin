package com.acme.transport.customs.inspection.inspectionentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient inspection mapping is in its life cycle. */
enum class LenientInspectionMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One lenient inspection mapping, with what the planners need to act on it. */
data class LenientInspectionMapping(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var visible: Boolean,
    var durationMinutes: Int,
    var odometer: Int,
    var rate: Double,
    var cost: Double,
    var retries: Int,
    var status: LenientInspectionMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientInspectionMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientInspectionMappingStatus): Boolean {
        when (status) {
            LenientInspectionMappingStatus.CREATED -> {
                when (next) {
                    LenientInspectionMappingStatus.PUBLISHED, LenientInspectionMappingStatus.RETIRED -> return true
                }
                return false
            }
            LenientInspectionMappingStatus.VALIDATED -> return next == LenientInspectionMappingStatus.PUBLISHED
            LenientInspectionMappingStatus.PUBLISHED -> return next == LenientInspectionMappingStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientInspectionMappingStatus) {
        if (!canMoveTo(next)) {
            error("lenient inspection mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveValue(): Double = round((durationMinutes.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): LenientInspectionMapping {
        return LenientInspectionMapping(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            visible = visible,
            durationMinutes = durationMinutes,
            odometer = odometer,
            rate = rate,
            cost = cost,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient inspection mapping in status created. */
fun newLenientInspectionMapping(code: String, title: String): LenientInspectionMapping {
    return LenientInspectionMapping(
        code = code,
        title = title,
        fuelLiters = 0.0,
        visible = false,
        durationMinutes = 0,
        odometer = 0,
        rate = 0.0,
        cost = 0.0,
        retries = 0,
        status = LenientInspectionMappingStatus.CREATED,
    )
}
