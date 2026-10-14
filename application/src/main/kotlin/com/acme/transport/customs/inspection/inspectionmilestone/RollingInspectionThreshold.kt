package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling inspection threshold is in its life cycle. */
enum class RollingInspectionThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling inspection threshold as the inspection team keeps it. */
data class RollingInspectionThreshold(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var vehicleId: String,
    var pinned: Boolean,
    var floorValue: Double,
    var routeCode: String,
    var etaMinutes: Int,
    var attributes: MutableMap<String, String>,
    var summary: String,
    var status: RollingInspectionThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingInspectionThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingInspectionThresholdStatus): Boolean {
        when (status) {
            RollingInspectionThresholdStatus.NEW -> return next == RollingInspectionThresholdStatus.QUEUED || next == RollingInspectionThresholdStatus.RUNNING
            RollingInspectionThresholdStatus.QUEUED -> return next == RollingInspectionThresholdStatus.DONE
            RollingInspectionThresholdStatus.RUNNING -> return next == RollingInspectionThresholdStatus.FAILED
            RollingInspectionThresholdStatus.DONE -> return next == RollingInspectionThresholdStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingInspectionThresholdStatus) {
        if (!canMoveTo(next)) {
            error("rolling inspection threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round((floorValue + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): RollingInspectionThreshold {
        return RollingInspectionThreshold(
            code = code,
            name = name,
            tags = tags,
            vehicleId = vehicleId,
            pinned = pinned,
            floorValue = floorValue,
            routeCode = routeCode,
            etaMinutes = etaMinutes,
            attributes = attributes,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling inspection threshold in status new. */
fun newRollingInspectionThreshold(code: String, name: String): RollingInspectionThreshold {
    return RollingInspectionThreshold(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        vehicleId = "",
        pinned = false,
        floorValue = 0.0,
        routeCode = "",
        etaMinutes = 0,
        attributes = mutableMapOf<String, String>(),
        summary = "",
        status = RollingInspectionThresholdStatus.NEW,
    )
}
