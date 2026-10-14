package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The camera event summary records of safety's camera event work, and the rules, stores, and calculations
// around them.

/** Where a camera event summary is in its life cycle. */
enum class CameraEventSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a camera event summary and its current status. */
data class CameraEventSummary(
    var code: String,
    var title: String,
    var region: String,
    var odometer: Int,
    var locale: String,
    var margin: Double,
    var channel: String,
    var quantity: Int,
    var floorValue: Double,
    var status: CameraEventSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventSummaryStatus): Boolean {
        when (status) {
            CameraEventSummaryStatus.REQUESTED -> return next == CameraEventSummaryStatus.REVOKED || next == CameraEventSummaryStatus.EXPIRED
            CameraEventSummaryStatus.GRANTED -> {
                when (next) {
                    CameraEventSummaryStatus.REVOKED, CameraEventSummaryStatus.EXPIRED -> return true
                }
                return false
            }
            CameraEventSummaryStatus.REVOKED -> {
                when (next) {
                    CameraEventSummaryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventSummaryStatus) {
        if (!canMoveTo(next)) {
            error("camera event summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"camera event summary"} $code"

    fun weightedExposure(): Double = round((odometer.toDouble() + floorValue) * 100) / 100

    fun snapshot(): CameraEventSummary {
        return CameraEventSummary(
            code = code,
            title = title,
            region = region,
            odometer = odometer,
            locale = locale,
            margin = margin,
            channel = channel,
            quantity = quantity,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a camera event summary in status requested. */
fun newCameraEventSummary(code: String, title: String): CameraEventSummary {
    return CameraEventSummary(
        code = code,
        title = title,
        region = "",
        odometer = 0,
        locale = "",
        margin = 0.0,
        channel = "",
        quantity = 0,
        floorValue = 0.0,
        status = CameraEventSummaryStatus.REQUESTED,
    )
}
