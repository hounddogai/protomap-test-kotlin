package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback incident slot is in its life cycle. */
enum class FallbackIncidentSlotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback incident slot. Its code is unique within a tenant. */
data class FallbackIncidentSlot(
    var code: String,
    var title: String,
    var depot: String,
    var odometer: Int,
    var rate: Double,
    var endsAt: Int,
    var etaMinutes: Int,
    var expiresAt: Int,
    var status: FallbackIncidentSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackIncidentSlotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackIncidentSlotStatus): Boolean {
        when (status) {
            FallbackIncidentSlotStatus.IDLE -> {
                when (next) {
                    FallbackIncidentSlotStatus.WARMING, FallbackIncidentSlotStatus.STOPPED -> return true
                }
                return false
            }
            FallbackIncidentSlotStatus.WARMING -> return next == FallbackIncidentSlotStatus.READY || next == FallbackIncidentSlotStatus.STOPPED
            FallbackIncidentSlotStatus.READY -> return next == FallbackIncidentSlotStatus.DRAINING || next == FallbackIncidentSlotStatus.STOPPED
            FallbackIncidentSlotStatus.DRAINING -> return next == FallbackIncidentSlotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackIncidentSlotStatus) {
        if (!canMoveTo(next)) {
            error("fallback incident slot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback incident slot"} $code"

    fun weightedVolume(): Double = round((endsAt.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): FallbackIncidentSlot {
        return FallbackIncidentSlot(
            code = code,
            title = title,
            depot = depot,
            odometer = odometer,
            rate = rate,
            endsAt = endsAt,
            etaMinutes = etaMinutes,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback incident slot in status idle. */
fun newFallbackIncidentSlot(code: String, title: String): FallbackIncidentSlot {
    return FallbackIncidentSlot(
        code = code,
        title = title,
        depot = "",
        odometer = 0,
        rate = 0.0,
        endsAt = 0,
        etaMinutes = 0,
        expiresAt = 0,
        status = FallbackIncidentSlotStatus.IDLE,
    )
}
