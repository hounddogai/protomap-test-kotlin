package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event forecast is in its life cycle. */
enum class CameraEventForecastStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A camera event forecast. Its code is unique within a tenant. */
data class CameraEventForecast(
    var code: String,
    var label: String,
    var checksum: String,
    var pallets: Int,
    var summary: String,
    var minimum: Double,
    var contactEmail: String,
    var discount: Double,
    var maximum: Double,
    var status: CameraEventForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventForecastStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventForecastStatus): Boolean {
        when (status) {
            CameraEventForecastStatus.NEW -> {
                when (next) {
                    CameraEventForecastStatus.RUNNING -> return true
                }
                return false
            }
            CameraEventForecastStatus.QUEUED -> {
                when (next) {
                    CameraEventForecastStatus.DONE -> return true
                }
                return false
            }
            CameraEventForecastStatus.RUNNING -> {
                when (next) {
                    CameraEventForecastStatus.DONE, CameraEventForecastStatus.FAILED -> return true
                }
                return false
            }
            CameraEventForecastStatus.DONE -> return next == CameraEventForecastStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CameraEventForecastStatus) {
        if (!canMoveTo(next)) {
            error("camera event forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"camera event forecast"} $code"

    fun effectiveExposure(): Double = round((pallets.toDouble() + minimum) * 100) / 100

    fun snapshot(): CameraEventForecast {
        return CameraEventForecast(
            code = code,
            label = label,
            checksum = checksum,
            pallets = pallets,
            summary = summary,
            minimum = minimum,
            contactEmail = contactEmail,
            discount = discount,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a camera event forecast in status new. */
fun newCameraEventForecast(code: String, label: String): CameraEventForecast {
    return CameraEventForecast(
        code = code,
        label = label,
        checksum = "",
        pallets = 0,
        summary = "",
        minimum = 0.0,
        contactEmail = "",
        discount = 0.0,
        maximum = 0.0,
        status = CameraEventForecastStatus.NEW,
    )
}
