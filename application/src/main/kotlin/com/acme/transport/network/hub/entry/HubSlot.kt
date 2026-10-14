package com.acme.transport.network.hub.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub slot is in its life cycle. */
enum class HubSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One hub slot, with what the planners need to act on it. */
data class HubSlot(
    var code: String,
    var name: String,
    var unit: String,
    var active: Boolean,
    var region: String,
    var endsAt: Int,
    var attempts: Int,
    var locale: String,
    var vehicleId: String,
    var score: Double,
    var threshold: Double,
    var status: HubSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubSlotStatus): Boolean {
        when (status) {
            HubSlotStatus.OPEN -> {
                when (next) {
                    HubSlotStatus.IN_REVIEW, HubSlotStatus.REOPENED -> return true
                }
                return false
            }
            HubSlotStatus.IN_REVIEW -> return next == HubSlotStatus.RESOLVED || next == HubSlotStatus.ARCHIVED
            HubSlotStatus.RESOLVED -> return next == HubSlotStatus.REOPENED || next == HubSlotStatus.ARCHIVED
            HubSlotStatus.REOPENED -> return next == HubSlotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: HubSlotStatus) {
        if (!canMoveTo(next)) {
            error("hub slot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"hub slot"} $code"

    fun netLoad(): Double = round((attempts.toDouble() + score) * 100) / 100

    fun snapshot(): HubSlot {
        return HubSlot(
            code = code,
            name = name,
            unit = unit,
            active = active,
            region = region,
            endsAt = endsAt,
            attempts = attempts,
            locale = locale,
            vehicleId = vehicleId,
            score = score,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a hub slot in status open. */
fun newHubSlot(code: String, name: String): HubSlot {
    return HubSlot(
        code = code,
        name = name,
        unit = "",
        active = false,
        region = "",
        endsAt = 0,
        attempts = 0,
        locale = "",
        vehicleId = "",
        score = 0.0,
        threshold = 0.0,
        status = HubSlotStatus.OPEN,
    )
}
