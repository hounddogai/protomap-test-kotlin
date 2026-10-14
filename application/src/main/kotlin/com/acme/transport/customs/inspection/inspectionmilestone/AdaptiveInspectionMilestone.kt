package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive inspection milestone records of customs's inspection work, and the rules, stores, and
// calculations around them.

/** Where a adaptive inspection milestone is in its life cycle. */
enum class AdaptiveInspectionMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A adaptive inspection milestone as the inspection team keeps it. */
data class AdaptiveInspectionMilestone(
    var code: String,
    var label: String,
    var summary: String,
    var country: String,
    var region: String,
    var maximum: Double,
    var createdAt: Int,
    var unit: String,
    var ratio: Double,
    var carrier: String,
    var checksum: String,
    var status: AdaptiveInspectionMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionMilestoneStatus): Boolean {
        when (status) {
            AdaptiveInspectionMilestoneStatus.CREATED -> return next == AdaptiveInspectionMilestoneStatus.VALIDATED || next == AdaptiveInspectionMilestoneStatus.PUBLISHED
            AdaptiveInspectionMilestoneStatus.VALIDATED -> return next == AdaptiveInspectionMilestoneStatus.RETIRED
            AdaptiveInspectionMilestoneStatus.PUBLISHED -> return next == AdaptiveInspectionMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection milestone $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"adaptive inspection milestone"} $code"

    fun netExposure(): Double = round((createdAt.toDouble() - maximum) * 100) / 100

    fun snapshot(): AdaptiveInspectionMilestone {
        return AdaptiveInspectionMilestone(
            code = code,
            label = label,
            summary = summary,
            country = country,
            region = region,
            maximum = maximum,
            createdAt = createdAt,
            unit = unit,
            ratio = ratio,
            carrier = carrier,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive inspection milestone in status created. */
fun newAdaptiveInspectionMilestone(code: String, label: String): AdaptiveInspectionMilestone {
    return AdaptiveInspectionMilestone(
        code = code,
        label = label,
        summary = "",
        country = "",
        region = "",
        maximum = 0.0,
        createdAt = 0,
        unit = "",
        ratio = 0.0,
        carrier = "",
        checksum = "",
        status = AdaptiveInspectionMilestoneStatus.CREATED,
    )
}
