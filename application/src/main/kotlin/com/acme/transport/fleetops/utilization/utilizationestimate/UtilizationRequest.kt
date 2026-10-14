package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization request is in its life cycle. */
enum class UtilizationRequestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a utilization request and its current status. */
data class UtilizationRequest(
    var code: String,
    var label: String,
    var carrier: String,
    var contactEmail: String,
    var region: String,
    var channel: String,
    var driverId: String,
    var attempts: Int,
    var etaMinutes: Int,
    var minimum: Double,
    var status: UtilizationRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationRequestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationRequestStatus): Boolean {
        when (status) {
            UtilizationRequestStatus.PLANNED -> {
                when (next) {
                    UtilizationRequestStatus.SCHEDULED, UtilizationRequestStatus.IN_PROGRESS -> return true
                }
                return false
            }
            UtilizationRequestStatus.SCHEDULED -> return next == UtilizationRequestStatus.IN_PROGRESS || next == UtilizationRequestStatus.CANCELLED
            UtilizationRequestStatus.IN_PROGRESS -> return next == UtilizationRequestStatus.COMPLETED
            UtilizationRequestStatus.COMPLETED -> return next == UtilizationRequestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: UtilizationRequestStatus) {
        if (!canMoveTo(next)) {
            error("utilization request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedValue(): Double = round(minimum * attempts.toDouble() * 100) / 100

    fun snapshot(): UtilizationRequest {
        return UtilizationRequest(
            code = code,
            label = label,
            carrier = carrier,
            contactEmail = contactEmail,
            region = region,
            channel = channel,
            driverId = driverId,
            attempts = attempts,
            etaMinutes = etaMinutes,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a utilization request in status planned. */
fun newUtilizationRequest(code: String, label: String): UtilizationRequest {
    return UtilizationRequest(
        code = code,
        label = label,
        carrier = "",
        contactEmail = "",
        region = "",
        channel = "",
        driverId = "",
        attempts = 0,
        etaMinutes = 0,
        minimum = 0.0,
        status = UtilizationRequestStatus.PLANNED,
    )
}
