package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event assignment is in its life cycle. */
enum class CameraEventAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One camera event assignment, with what the planners need to act on it. */
data class CameraEventAssignment(
    var code: String,
    var label: String,
    var shiftCode: String,
    var country: String,
    var maximum: Double,
    var rate: Double,
    var externalRef: String,
    var priority: Int,
    var etaMinutes: Int,
    var status: CameraEventAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventAssignmentStatus): Boolean {
        when (status) {
            CameraEventAssignmentStatus.CREATED -> return next == CameraEventAssignmentStatus.PUBLISHED
            CameraEventAssignmentStatus.VALIDATED -> {
                when (next) {
                    CameraEventAssignmentStatus.PUBLISHED -> return true
                }
                return false
            }
            CameraEventAssignmentStatus.PUBLISHED -> return next == CameraEventAssignmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CameraEventAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("camera event assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedVolume(): Double = round((rate - maximum) * 100) / 100

    fun snapshot(): CameraEventAssignment {
        return CameraEventAssignment(
            code = code,
            label = label,
            shiftCode = shiftCode,
            country = country,
            maximum = maximum,
            rate = rate,
            externalRef = externalRef,
            priority = priority,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a camera event assignment in status created. */
fun newCameraEventAssignment(code: String, label: String): CameraEventAssignment {
    return CameraEventAssignment(
        code = code,
        label = label,
        shiftCode = "",
        country = "",
        maximum = 0.0,
        rate = 0.0,
        externalRef = "",
        priority = 0,
        etaMinutes = 0,
        status = CameraEventAssignmentStatus.CREATED,
    )
}
