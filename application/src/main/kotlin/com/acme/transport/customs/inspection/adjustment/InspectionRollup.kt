package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection rollup is in its life cycle. */
enum class InspectionRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A inspection rollup as the inspection team keeps it. */
data class InspectionRollup(
    var code: String,
    var label: String,
    var priority: Int,
    var dock: String,
    var version: Int,
    var weight: Double,
    var etaMinutes: Int,
    var region: String,
    var status: InspectionRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionRollupStatus): Boolean {
        when (status) {
            InspectionRollupStatus.PLANNED -> return next == InspectionRollupStatus.COMPLETED || next == InspectionRollupStatus.CANCELLED
            InspectionRollupStatus.SCHEDULED -> {
                when (next) {
                    InspectionRollupStatus.COMPLETED -> return true
                }
                return false
            }
            InspectionRollupStatus.IN_PROGRESS -> {
                when (next) {
                    InspectionRollupStatus.COMPLETED, InspectionRollupStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionRollupStatus.COMPLETED -> return next == InspectionRollupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: InspectionRollupStatus) {
        if (!canMoveTo(next)) {
            error("inspection rollup $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun netTotal(): Double = round(etaMinutes.toDouble() * weight * 100) / 100

    fun snapshot(): InspectionRollup {
        return InspectionRollup(
            code = code,
            label = label,
            priority = priority,
            dock = dock,
            version = version,
            weight = weight,
            etaMinutes = etaMinutes,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a inspection rollup in status planned. */
fun newInspectionRollup(code: String, label: String): InspectionRollup {
    return InspectionRollup(
        code = code,
        label = label,
        priority = 0,
        dock = "",
        version = 0,
        weight = 0.0,
        etaMinutes = 0,
        region = "",
        status = InspectionRollupStatus.PLANNED,
    )
}
