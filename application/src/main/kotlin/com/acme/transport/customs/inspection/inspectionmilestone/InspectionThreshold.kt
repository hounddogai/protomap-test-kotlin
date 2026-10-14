package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection threshold is in its life cycle. */
enum class InspectionThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One inspection threshold, with what the planners need to act on it. */
data class InspectionThreshold(
    var code: String,
    var name: String,
    var floorValue: Double,
    var active: Boolean,
    var margin: Double,
    var sequence: Int,
    var version: Int,
    var tags: MutableList<String>,
    var ownerId: String,
    var status: InspectionThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionThresholdStatus): Boolean {
        when (status) {
            InspectionThresholdStatus.PROPOSED -> {
                when (next) {
                    InspectionThresholdStatus.REJECTED, InspectionThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            InspectionThresholdStatus.APPROVED -> {
                when (next) {
                    InspectionThresholdStatus.REJECTED, InspectionThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            InspectionThresholdStatus.REJECTED -> return next == InspectionThresholdStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: InspectionThresholdStatus) {
        if (!canMoveTo(next)) {
            error("inspection threshold $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"inspection threshold"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round(version.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): InspectionThreshold {
        return InspectionThreshold(
            code = code,
            name = name,
            floorValue = floorValue,
            active = active,
            margin = margin,
            sequence = sequence,
            version = version,
            tags = tags,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a inspection threshold in status proposed. */
fun newInspectionThreshold(code: String, name: String): InspectionThreshold {
    return InspectionThreshold(
        code = code,
        name = name,
        floorValue = 0.0,
        active = false,
        margin = 0.0,
        sequence = 0,
        version = 0,
        tags = mutableListOf<String>(),
        ownerId = "",
        status = InspectionThresholdStatus.PROPOSED,
    )
}
