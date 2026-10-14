package com.acme.transport.fleetops.defect.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect segment is in its life cycle. */
enum class DefectSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a defect segment and its current status. */
data class DefectSegment(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var distanceKm: Double,
    var trailerId: String,
    var attributes: MutableMap<String, String>,
    var shiftCode: String,
    var stopCount: Int,
    var status: DefectSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectSegmentStatus): Boolean {
        when (status) {
            DefectSegmentStatus.PROPOSED -> {
                when (next) {
                    DefectSegmentStatus.APPROVED, DefectSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefectSegmentStatus.APPROVED -> return next == DefectSegmentStatus.WITHDRAWN
            DefectSegmentStatus.REJECTED -> return next == DefectSegmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefectSegmentStatus) {
        if (!canMoveTo(next)) {
            error("defect segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round(fuelLiters * stopCount.toDouble() * 100) / 100

    fun snapshot(): DefectSegment {
        return DefectSegment(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            distanceKm = distanceKm,
            trailerId = trailerId,
            attributes = attributes,
            shiftCode = shiftCode,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a defect segment in status proposed. */
fun newDefectSegment(code: String, label: String): DefectSegment {
    return DefectSegment(
        code = code,
        label = label,
        fuelLiters = 0.0,
        distanceKm = 0.0,
        trailerId = "",
        attributes = mutableMapOf<String, String>(),
        shiftCode = "",
        stopCount = 0,
        status = DefectSegmentStatus.PROPOSED,
    )
}
