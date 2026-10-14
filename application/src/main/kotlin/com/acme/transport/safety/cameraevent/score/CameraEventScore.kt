package com.acme.transport.safety.cameraevent.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The camera event score records of safety's camera event work, and the rules, stores, and calculations
// around them.

/** Where a camera event score is in its life cycle. */
enum class CameraEventScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a camera event score and its current status. */
data class CameraEventScore(
    var code: String,
    var name: String,
    var trailerId: String,
    var amount: Double,
    var ratio: Double,
    var loadKg: Double,
    var pinned: Boolean,
    var margin: Double,
    var minimum: Double,
    var vehicleId: String,
    var status: CameraEventScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventScoreStatus): Boolean {
        when (status) {
            CameraEventScoreStatus.PLANNED -> {
                when (next) {
                    CameraEventScoreStatus.IN_PROGRESS, CameraEventScoreStatus.COMPLETED -> return true
                }
                return false
            }
            CameraEventScoreStatus.SCHEDULED -> return next == CameraEventScoreStatus.IN_PROGRESS || next == CameraEventScoreStatus.CANCELLED
            CameraEventScoreStatus.IN_PROGRESS -> {
                when (next) {
                    CameraEventScoreStatus.COMPLETED -> return true
                }
                return false
            }
            CameraEventScoreStatus.COMPLETED -> {
                when (next) {
                    CameraEventScoreStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventScoreStatus) {
        if (!canMoveTo(next)) {
            error("camera event score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedTotal(): Double = round((amount + margin) * 100) / 100

    fun snapshot(): CameraEventScore {
        return CameraEventScore(
            code = code,
            name = name,
            trailerId = trailerId,
            amount = amount,
            ratio = ratio,
            loadKg = loadKg,
            pinned = pinned,
            margin = margin,
            minimum = minimum,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a camera event score in status planned. */
fun newCameraEventScore(code: String, name: String): CameraEventScore {
    return CameraEventScore(
        code = code,
        name = name,
        trailerId = "",
        amount = 0.0,
        ratio = 0.0,
        loadKg = 0.0,
        pinned = false,
        margin = 0.0,
        minimum = 0.0,
        vehicleId = "",
        status = CameraEventScoreStatus.PLANNED,
    )
}
