package com.acme.transport.fleetops.inspection.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection adjustment records of fleetops's inspection work, and the rules, stores, and calculations
// around them.

/** Where a inspection adjustment is in its life cycle. */
enum class InspectionAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A inspection adjustment as the inspection team keeps it. */
data class InspectionAdjustment(
    var code: String,
    var title: String,
    var visible: Boolean,
    var summary: String,
    var budget: Double,
    var channel: String,
    var contactEmail: String,
    var distanceKm: Double,
    var tags: MutableList<String>,
    var status: InspectionAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionAdjustmentStatus): Boolean {
        when (status) {
            InspectionAdjustmentStatus.NEW -> return next == InspectionAdjustmentStatus.QUEUED || next == InspectionAdjustmentStatus.RUNNING
            InspectionAdjustmentStatus.QUEUED -> {
                when (next) {
                    InspectionAdjustmentStatus.RUNNING, InspectionAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            InspectionAdjustmentStatus.RUNNING -> return next == InspectionAdjustmentStatus.FAILED
            InspectionAdjustmentStatus.DONE -> return next == InspectionAdjustmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: InspectionAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("inspection adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"inspection adjustment"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round((distanceKm + budget) * 100) / 100

    fun snapshot(): InspectionAdjustment {
        return InspectionAdjustment(
            code = code,
            title = title,
            visible = visible,
            summary = summary,
            budget = budget,
            channel = channel,
            contactEmail = contactEmail,
            distanceKm = distanceKm,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a inspection adjustment in status new. */
fun newInspectionAdjustment(code: String, title: String): InspectionAdjustment {
    return InspectionAdjustment(
        code = code,
        title = title,
        visible = false,
        summary = "",
        budget = 0.0,
        channel = "",
        contactEmail = "",
        distanceKm = 0.0,
        tags = mutableListOf<String>(),
        status = InspectionAdjustmentStatus.NEW,
    )
}
