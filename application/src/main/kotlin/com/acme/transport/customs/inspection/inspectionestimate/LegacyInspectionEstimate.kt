package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy inspection estimate records of customs's inspection work, and the rules, stores, and
// calculations around them.

/** Where a legacy inspection estimate is in its life cycle. */
enum class LegacyInspectionEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a legacy inspection estimate and its current status. */
data class LegacyInspectionEstimate(
    var code: String,
    var label: String,
    var name: String,
    var updatedAt: Int,
    var weight: Double,
    var cost: Double,
    var quantity: Int,
    var title: String,
    var version: Int,
    var visible: Boolean,
    var category: String,
    var status: LegacyInspectionEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyInspectionEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyInspectionEstimateStatus): Boolean {
        when (status) {
            LegacyInspectionEstimateStatus.IDLE -> return next == LegacyInspectionEstimateStatus.WARMING || next == LegacyInspectionEstimateStatus.DRAINING
            LegacyInspectionEstimateStatus.WARMING -> {
                when (next) {
                    LegacyInspectionEstimateStatus.STOPPED -> return true
                }
                return false
            }
            LegacyInspectionEstimateStatus.READY -> {
                when (next) {
                    LegacyInspectionEstimateStatus.DRAINING -> return true
                }
                return false
            }
            LegacyInspectionEstimateStatus.DRAINING -> return next == LegacyInspectionEstimateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacyInspectionEstimateStatus) {
        if (!canMoveTo(next)) {
            error("legacy inspection estimate $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedLoad(): Double = round((quantity.toDouble() - weight) * 100) / 100

    fun snapshot(): LegacyInspectionEstimate {
        return LegacyInspectionEstimate(
            code = code,
            label = label,
            name = name,
            updatedAt = updatedAt,
            weight = weight,
            cost = cost,
            quantity = quantity,
            title = title,
            version = version,
            visible = visible,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a legacy inspection estimate in status idle. */
fun newLegacyInspectionEstimate(code: String, label: String): LegacyInspectionEstimate {
    return LegacyInspectionEstimate(
        code = code,
        label = label,
        name = "",
        updatedAt = 0,
        weight = 0.0,
        cost = 0.0,
        quantity = 0,
        title = "",
        version = 0,
        visible = false,
        category = "",
        status = LegacyInspectionEstimateStatus.IDLE,
    )
}
