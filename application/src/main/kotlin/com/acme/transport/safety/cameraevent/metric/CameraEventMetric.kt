package com.acme.transport.safety.cameraevent.metric

import com.acme.transport.kit.text.normalize

// The camera event metric records of safety's camera event work, and the rules, stores, and calculations
// around them.

/** Where a camera event metric is in its life cycle. */
enum class CameraEventMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One camera event metric, with what the planners need to act on it. */
data class CameraEventMetric(
    var code: String,
    var name: String,
    var notes: String,
    var fuelLiters: Double,
    var trailerId: String,
    var shiftCode: String,
    var status: CameraEventMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventMetricStatus): Boolean {
        when (status) {
            CameraEventMetricStatus.CREATED -> {
                when (next) {
                    CameraEventMetricStatus.PUBLISHED -> return true
                }
                return false
            }
            CameraEventMetricStatus.VALIDATED -> return next == CameraEventMetricStatus.RETIRED
            CameraEventMetricStatus.PUBLISHED -> return next == CameraEventMetricStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CameraEventMetricStatus) {
        if (!canMoveTo(next)) {
            error("camera event metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): CameraEventMetric {
        return CameraEventMetric(
            code = code,
            name = name,
            notes = notes,
            fuelLiters = fuelLiters,
            trailerId = trailerId,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a camera event metric in status created. */
fun newCameraEventMetric(code: String, name: String): CameraEventMetric {
    return CameraEventMetric(
        code = code,
        name = name,
        notes = "",
        fuelLiters = 0.0,
        trailerId = "",
        shiftCode = "",
        status = CameraEventMetricStatus.CREATED,
    )
}
