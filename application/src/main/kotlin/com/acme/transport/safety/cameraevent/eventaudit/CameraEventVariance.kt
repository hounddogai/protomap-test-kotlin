package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event variance is in its life cycle. */
enum class CameraEventVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a camera event variance and its current status. */
data class CameraEventVariance(
    var code: String,
    var name: String,
    var revision: Int,
    var unit: String,
    var archived: Boolean,
    var reviewer: String,
    var budget: Double,
    var region: String,
    var status: CameraEventVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventVarianceStatus): Boolean {
        when (status) {
            CameraEventVarianceStatus.IDLE -> {
                when (next) {
                    CameraEventVarianceStatus.DRAINING, CameraEventVarianceStatus.STOPPED -> return true
                }
                return false
            }
            CameraEventVarianceStatus.WARMING -> {
                when (next) {
                    CameraEventVarianceStatus.READY, CameraEventVarianceStatus.STOPPED -> return true
                }
                return false
            }
            CameraEventVarianceStatus.READY -> return next == CameraEventVarianceStatus.STOPPED
            CameraEventVarianceStatus.DRAINING -> return next == CameraEventVarianceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CameraEventVarianceStatus) {
        if (!canMoveTo(next)) {
            error("camera event variance $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"camera event variance"} $code"

    fun adjustedTotal(): Double = round((budget + revision.toDouble()) * 100) / 100

    fun snapshot(): CameraEventVariance {
        return CameraEventVariance(
            code = code,
            name = name,
            revision = revision,
            unit = unit,
            archived = archived,
            reviewer = reviewer,
            budget = budget,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a camera event variance in status idle. */
fun newCameraEventVariance(code: String, name: String): CameraEventVariance {
    return CameraEventVariance(
        code = code,
        name = name,
        revision = 0,
        unit = "",
        archived = false,
        reviewer = "",
        budget = 0.0,
        region = "",
        status = CameraEventVarianceStatus.IDLE,
    )
}
