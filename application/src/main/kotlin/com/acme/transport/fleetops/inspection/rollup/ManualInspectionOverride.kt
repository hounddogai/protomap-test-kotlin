package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual inspection override is in its life cycle. */
enum class ManualInspectionOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A manual inspection override as the inspection team keeps it. */
data class ManualInspectionOverride(
    var code: String,
    var name: String,
    var rate: Double,
    var floorValue: Double,
    var limit: Int,
    var status: ManualInspectionOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualInspectionOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualInspectionOverrideStatus): Boolean {
        when (status) {
            ManualInspectionOverrideStatus.OPEN -> {
                when (next) {
                    ManualInspectionOverrideStatus.RESOLVED, ManualInspectionOverrideStatus.REOPENED -> return true
                }
                return false
            }
            ManualInspectionOverrideStatus.IN_REVIEW -> {
                when (next) {
                    ManualInspectionOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualInspectionOverrideStatus.RESOLVED -> {
                when (next) {
                    ManualInspectionOverrideStatus.REOPENED -> return true
                }
                return false
            }
            ManualInspectionOverrideStatus.REOPENED -> {
                when (next) {
                    ManualInspectionOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualInspectionOverrideStatus) {
        if (!canMoveTo(next)) {
            error("manual inspection override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual inspection override"} $code"

    fun weightedExposure(): Double = round(limit.toDouble() * floorValue * 100) / 100

    fun snapshot(): ManualInspectionOverride {
        return ManualInspectionOverride(
            code = code,
            name = name,
            rate = rate,
            floorValue = floorValue,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual inspection override in status open. */
fun newManualInspectionOverride(code: String, name: String): ManualInspectionOverride {
    return ManualInspectionOverride(
        code = code,
        name = name,
        rate = 0.0,
        floorValue = 0.0,
        limit = 0,
        status = ManualInspectionOverrideStatus.OPEN,
    )
}
