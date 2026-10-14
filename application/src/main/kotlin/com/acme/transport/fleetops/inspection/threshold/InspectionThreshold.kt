package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.text.normalize

// The inspection threshold records of fleetops's inspection work, and the rules, stores, and calculations
// around them.

/** Where a inspection threshold is in its life cycle. */
enum class InspectionThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a inspection threshold and its current status. */
data class InspectionThreshold(
    var code: String,
    var title: String,
    var driverId: String,
    var active: Boolean,
    var source: String,
    var minimum: Double,
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
            InspectionThresholdStatus.PROPOSED -> return next == InspectionThresholdStatus.APPROVED || next == InspectionThresholdStatus.WITHDRAWN
            InspectionThresholdStatus.APPROVED -> return next == InspectionThresholdStatus.REJECTED || next == InspectionThresholdStatus.WITHDRAWN
            InspectionThresholdStatus.REJECTED -> return next == InspectionThresholdStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: InspectionThresholdStatus) {
        if (!canMoveTo(next)) {
            error("inspection threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = minimum

    fun snapshot(): InspectionThreshold {
        return InspectionThreshold(
            code = code,
            title = title,
            driverId = driverId,
            active = active,
            source = source,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a inspection threshold in status proposed. */
fun newInspectionThreshold(code: String, title: String): InspectionThreshold {
    return InspectionThreshold(
        code = code,
        title = title,
        driverId = "",
        active = false,
        source = "",
        minimum = 0.0,
        status = InspectionThresholdStatus.PROPOSED,
    )
}
