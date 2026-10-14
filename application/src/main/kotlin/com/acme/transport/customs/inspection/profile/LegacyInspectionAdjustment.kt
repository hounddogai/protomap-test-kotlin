package com.acme.transport.customs.inspection.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy inspection adjustment is in its life cycle. */
enum class LegacyInspectionAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One legacy inspection adjustment, with what the planners need to act on it. */
data class LegacyInspectionAdjustment(
    var code: String,
    var title: String,
    var version: Int,
    var margin: Double,
    var shiftCode: String,
    var limit: Int,
    var rate: Double,
    var status: LegacyInspectionAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyInspectionAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyInspectionAdjustmentStatus): Boolean {
        when (status) {
            LegacyInspectionAdjustmentStatus.CREATED -> return next == LegacyInspectionAdjustmentStatus.PUBLISHED
            LegacyInspectionAdjustmentStatus.VALIDATED -> {
                when (next) {
                    LegacyInspectionAdjustmentStatus.PUBLISHED -> return true
                }
                return false
            }
            LegacyInspectionAdjustmentStatus.PUBLISHED -> return next == LegacyInspectionAdjustmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LegacyInspectionAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("legacy inspection adjustment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun adjustedValue(): Double = round((margin - rate) * 100) / 100

    fun snapshot(): LegacyInspectionAdjustment {
        return LegacyInspectionAdjustment(
            code = code,
            title = title,
            version = version,
            margin = margin,
            shiftCode = shiftCode,
            limit = limit,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy inspection adjustment in status created. */
fun newLegacyInspectionAdjustment(code: String, title: String): LegacyInspectionAdjustment {
    return LegacyInspectionAdjustment(
        code = code,
        title = title,
        version = 0,
        margin = 0.0,
        shiftCode = "",
        limit = 0,
        rate = 0.0,
        status = LegacyInspectionAdjustmentStatus.CREATED,
    )
}
