package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.kit.text.normalize

/** Where a global inspection template is in its life cycle. */
enum class GlobalInspectionTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A global inspection template as the inspection team keeps it. */
data class GlobalInspectionTemplate(
    var code: String,
    var name: String,
    var country: String,
    var floorValue: Double,
    var reviewer: String,
    var status: GlobalInspectionTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalInspectionTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalInspectionTemplateStatus): Boolean {
        when (status) {
            GlobalInspectionTemplateStatus.PLANNED -> {
                when (next) {
                    GlobalInspectionTemplateStatus.SCHEDULED -> return true
                }
                return false
            }
            GlobalInspectionTemplateStatus.SCHEDULED -> return next == GlobalInspectionTemplateStatus.IN_PROGRESS
            GlobalInspectionTemplateStatus.IN_PROGRESS -> return next == GlobalInspectionTemplateStatus.COMPLETED || next == GlobalInspectionTemplateStatus.CANCELLED
            GlobalInspectionTemplateStatus.COMPLETED -> {
                when (next) {
                    GlobalInspectionTemplateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalInspectionTemplateStatus) {
        if (!canMoveTo(next)) {
            error("global inspection template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = floorValue

    fun snapshot(): GlobalInspectionTemplate {
        return GlobalInspectionTemplate(
            code = code,
            name = name,
            country = country,
            floorValue = floorValue,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global inspection template in status planned. */
fun newGlobalInspectionTemplate(code: String, name: String): GlobalInspectionTemplate {
    return GlobalInspectionTemplate(
        code = code,
        name = name,
        country = "",
        floorValue = 0.0,
        reviewer = "",
        status = GlobalInspectionTemplateStatus.PLANNED,
    )
}
