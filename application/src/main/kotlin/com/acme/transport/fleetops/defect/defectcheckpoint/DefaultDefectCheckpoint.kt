package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default defect checkpoint records of fleetops's defect work, and the rules, stores, and calculations
// around them.

/** Where a default defect checkpoint is in its life cycle. */
enum class DefaultDefectCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a default defect checkpoint and its current status. */
data class DefaultDefectCheckpoint(
    var code: String,
    var title: String,
    var endsAt: Int,
    var odometer: Int,
    var margin: Double,
    var shiftCode: String,
    var amount: Double,
    var active: Boolean,
    var durationMinutes: Int,
    var etaMinutes: Int,
    var status: DefaultDefectCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDefectCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDefectCheckpointStatus): Boolean {
        when (status) {
            DefaultDefectCheckpointStatus.IDLE -> {
                when (next) {
                    DefaultDefectCheckpointStatus.WARMING -> return true
                }
                return false
            }
            DefaultDefectCheckpointStatus.WARMING -> return next == DefaultDefectCheckpointStatus.DRAINING
            DefaultDefectCheckpointStatus.READY -> return next == DefaultDefectCheckpointStatus.DRAINING || next == DefaultDefectCheckpointStatus.STOPPED
            DefaultDefectCheckpointStatus.DRAINING -> return next == DefaultDefectCheckpointStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultDefectCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("default defect checkpoint $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default defect checkpoint"} $code"

    fun effectiveLoad(): Double = round((odometer.toDouble() - margin) * 100) / 100

    fun snapshot(): DefaultDefectCheckpoint {
        return DefaultDefectCheckpoint(
            code = code,
            title = title,
            endsAt = endsAt,
            odometer = odometer,
            margin = margin,
            shiftCode = shiftCode,
            amount = amount,
            active = active,
            durationMinutes = durationMinutes,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default defect checkpoint in status idle. */
fun newDefaultDefectCheckpoint(code: String, title: String): DefaultDefectCheckpoint {
    return DefaultDefectCheckpoint(
        code = code,
        title = title,
        endsAt = 0,
        odometer = 0,
        margin = 0.0,
        shiftCode = "",
        amount = 0.0,
        active = false,
        durationMinutes = 0,
        etaMinutes = 0,
        status = DefaultDefectCheckpointStatus.IDLE,
    )
}
