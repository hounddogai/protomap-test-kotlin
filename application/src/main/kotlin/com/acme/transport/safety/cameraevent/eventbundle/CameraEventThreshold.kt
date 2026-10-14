package com.acme.transport.safety.cameraevent.eventbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event threshold is in its life cycle. */
enum class CameraEventThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One camera event threshold, with what the planners need to act on it. */
data class CameraEventThreshold(
    var code: String,
    var label: String,
    var amount: Double,
    var country: String,
    var limit: Int,
    var minimum: Double,
    var locale: String,
    var channel: String,
    var batchSize: Int,
    var status: CameraEventThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventThresholdStatus): Boolean {
        when (status) {
            CameraEventThresholdStatus.NEW -> return next == CameraEventThresholdStatus.DONE || next == CameraEventThresholdStatus.FAILED
            CameraEventThresholdStatus.QUEUED -> {
                when (next) {
                    CameraEventThresholdStatus.RUNNING -> return true
                }
                return false
            }
            CameraEventThresholdStatus.RUNNING -> return next == CameraEventThresholdStatus.FAILED
            CameraEventThresholdStatus.DONE -> return next == CameraEventThresholdStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CameraEventThresholdStatus) {
        if (!canMoveTo(next)) {
            error("camera event threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"camera event threshold"} $code"

    fun weightedExposure(): Double = round((batchSize.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): CameraEventThreshold {
        return CameraEventThreshold(
            code = code,
            label = label,
            amount = amount,
            country = country,
            limit = limit,
            minimum = minimum,
            locale = locale,
            channel = channel,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a camera event threshold in status new. */
fun newCameraEventThreshold(code: String, label: String): CameraEventThreshold {
    return CameraEventThreshold(
        code = code,
        label = label,
        amount = 0.0,
        country = "",
        limit = 0,
        minimum = 0.0,
        locale = "",
        channel = "",
        batchSize = 0,
        status = CameraEventThresholdStatus.NEW,
    )
}
