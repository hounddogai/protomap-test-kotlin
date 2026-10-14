package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global inspection group is in its life cycle. */
enum class GlobalInspectionGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a global inspection group and its current status. */
data class GlobalInspectionGroup(
    var code: String,
    var label: String,
    var ceiling: Double,
    var approved: Boolean,
    var cost: Double,
    var locale: String,
    var segment: String,
    var trailerId: String,
    var rate: Double,
    var status: GlobalInspectionGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalInspectionGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalInspectionGroupStatus): Boolean {
        when (status) {
            GlobalInspectionGroupStatus.PLANNED -> {
                when (next) {
                    GlobalInspectionGroupStatus.IN_PROGRESS -> return true
                }
                return false
            }
            GlobalInspectionGroupStatus.SCHEDULED -> {
                when (next) {
                    GlobalInspectionGroupStatus.IN_PROGRESS, GlobalInspectionGroupStatus.COMPLETED -> return true
                }
                return false
            }
            GlobalInspectionGroupStatus.IN_PROGRESS -> return next == GlobalInspectionGroupStatus.CANCELLED
            GlobalInspectionGroupStatus.COMPLETED -> {
                when (next) {
                    GlobalInspectionGroupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalInspectionGroupStatus) {
        if (!canMoveTo(next)) {
            error("global inspection group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global inspection group"} $code"

    fun netTotal(): Double = round(cost * ceiling * 100) / 100

    fun snapshot(): GlobalInspectionGroup {
        return GlobalInspectionGroup(
            code = code,
            label = label,
            ceiling = ceiling,
            approved = approved,
            cost = cost,
            locale = locale,
            segment = segment,
            trailerId = trailerId,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a global inspection group in status planned. */
fun newGlobalInspectionGroup(code: String, label: String): GlobalInspectionGroup {
    return GlobalInspectionGroup(
        code = code,
        label = label,
        ceiling = 0.0,
        approved = false,
        cost = 0.0,
        locale = "",
        segment = "",
        trailerId = "",
        rate = 0.0,
        status = GlobalInspectionGroupStatus.PLANNED,
    )
}
