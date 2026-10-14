package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global inspection plan records of customs's inspection work, and the rules, stores, and calculations
// around them.

/** Where a global inspection plan is in its life cycle. */
enum class GlobalInspectionPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A global inspection plan. Its code is unique within a tenant. */
data class GlobalInspectionPlan(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var limit: Int,
    var ownerId: String,
    var name: String,
    var attempts: Int,
    var notes: String,
    var routeCode: String,
    var attributes: MutableMap<String, String>,
    var windowDays: Int,
    var status: GlobalInspectionPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalInspectionPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalInspectionPlanStatus): Boolean {
        when (status) {
            GlobalInspectionPlanStatus.PLANNED -> return next == GlobalInspectionPlanStatus.IN_PROGRESS
            GlobalInspectionPlanStatus.SCHEDULED -> {
                when (next) {
                    GlobalInspectionPlanStatus.COMPLETED, GlobalInspectionPlanStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalInspectionPlanStatus.IN_PROGRESS -> {
                when (next) {
                    GlobalInspectionPlanStatus.COMPLETED, GlobalInspectionPlanStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalInspectionPlanStatus.COMPLETED -> {
                when (next) {
                    GlobalInspectionPlanStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalInspectionPlanStatus) {
        if (!canMoveTo(next)) {
            error("global inspection plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round(limit.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): GlobalInspectionPlan {
        return GlobalInspectionPlan(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            limit = limit,
            ownerId = ownerId,
            name = name,
            attempts = attempts,
            notes = notes,
            routeCode = routeCode,
            attributes = attributes,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global inspection plan in status planned. */
fun newGlobalInspectionPlan(code: String, label: String): GlobalInspectionPlan {
    return GlobalInspectionPlan(
        code = code,
        label = label,
        durationMinutes = 0,
        limit = 0,
        ownerId = "",
        name = "",
        attempts = 0,
        notes = "",
        routeCode = "",
        attributes = mutableMapOf<String, String>(),
        windowDays = 0,
        status = GlobalInspectionPlanStatus.PLANNED,
    )
}
