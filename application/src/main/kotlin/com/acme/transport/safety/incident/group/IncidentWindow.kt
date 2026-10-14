package com.acme.transport.safety.incident.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident window is in its life cycle. */
enum class IncidentWindowStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a incident window and its current status. */
data class IncidentWindow(
    var code: String,
    var label: String,
    var threshold: Double,
    var driverId: String,
    var quantity: Int,
    var status: IncidentWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentWindowStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentWindowStatus): Boolean {
        when (status) {
            IncidentWindowStatus.REQUESTED -> return next == IncidentWindowStatus.GRANTED
            IncidentWindowStatus.GRANTED -> {
                when (next) {
                    IncidentWindowStatus.EXPIRED -> return true
                }
                return false
            }
            IncidentWindowStatus.REVOKED -> return next == IncidentWindowStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: IncidentWindowStatus) {
        if (!canMoveTo(next)) {
            error("incident window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"incident window"} $code"

    fun effectiveLoad(): Double = round(threshold * quantity.toDouble() * 100) / 100

    fun snapshot(): IncidentWindow {
        return IncidentWindow(
            code = code,
            label = label,
            threshold = threshold,
            driverId = driverId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a incident window in status requested. */
fun newIncidentWindow(code: String, label: String): IncidentWindow {
    return IncidentWindow(
        code = code,
        label = label,
        threshold = 0.0,
        driverId = "",
        quantity = 0,
        status = IncidentWindowStatus.REQUESTED,
    )
}
