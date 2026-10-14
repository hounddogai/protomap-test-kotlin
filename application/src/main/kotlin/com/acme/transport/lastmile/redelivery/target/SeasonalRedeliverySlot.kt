package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal redelivery slot is in its life cycle. */
enum class SeasonalRedeliverySlotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal redelivery slot. Its code is unique within a tenant. */
data class SeasonalRedeliverySlot(
    var code: String,
    var title: String,
    var label: String,
    var updatedAt: Int,
    var description: String,
    var margin: Double,
    var vehicleId: String,
    var carrier: String,
    var status: SeasonalRedeliverySlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRedeliverySlotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRedeliverySlotStatus): Boolean {
        when (status) {
            SeasonalRedeliverySlotStatus.IDLE -> return next == SeasonalRedeliverySlotStatus.WARMING
            SeasonalRedeliverySlotStatus.WARMING -> return next == SeasonalRedeliverySlotStatus.DRAINING || next == SeasonalRedeliverySlotStatus.STOPPED
            SeasonalRedeliverySlotStatus.READY -> return next == SeasonalRedeliverySlotStatus.DRAINING
            SeasonalRedeliverySlotStatus.DRAINING -> {
                when (next) {
                    SeasonalRedeliverySlotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRedeliverySlotStatus) {
        if (!canMoveTo(next)) {
            error("seasonal redelivery slot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $title"

    fun effectiveValue(): Double = round((updatedAt.toDouble() - margin) * 100) / 100

    fun snapshot(): SeasonalRedeliverySlot {
        return SeasonalRedeliverySlot(
            code = code,
            title = title,
            label = label,
            updatedAt = updatedAt,
            description = description,
            margin = margin,
            vehicleId = vehicleId,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal redelivery slot in status idle. */
fun newSeasonalRedeliverySlot(code: String, title: String): SeasonalRedeliverySlot {
    return SeasonalRedeliverySlot(
        code = code,
        title = title,
        label = "",
        updatedAt = 0,
        description = "",
        margin = 0.0,
        vehicleId = "",
        carrier = "",
        status = SeasonalRedeliverySlotStatus.IDLE,
    )
}
