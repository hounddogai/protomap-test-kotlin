package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global availability slot is in its life cycle. */
enum class GlobalAvailabilitySlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global availability slot as the availability team keeps it. */
data class GlobalAvailabilitySlot(
    var code: String,
    var name: String,
    var startsAt: Int,
    var label: String,
    var carrier: String,
    var channel: String,
    var rate: Double,
    var revision: Int,
    var maximum: Double,
    var dock: String,
    var status: GlobalAvailabilitySlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAvailabilitySlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAvailabilitySlotStatus): Boolean {
        when (status) {
            GlobalAvailabilitySlotStatus.PROPOSED -> return next == GlobalAvailabilitySlotStatus.WITHDRAWN
            GlobalAvailabilitySlotStatus.APPROVED -> {
                when (next) {
                    GlobalAvailabilitySlotStatus.REJECTED, GlobalAvailabilitySlotStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalAvailabilitySlotStatus.REJECTED -> {
                when (next) {
                    GlobalAvailabilitySlotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalAvailabilitySlotStatus) {
        if (!canMoveTo(next)) {
            error("global availability slot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($name)"

    fun effectiveValue(): Double = round((startsAt.toDouble() - maximum) * 100) / 100

    fun snapshot(): GlobalAvailabilitySlot {
        return GlobalAvailabilitySlot(
            code = code,
            name = name,
            startsAt = startsAt,
            label = label,
            carrier = carrier,
            channel = channel,
            rate = rate,
            revision = revision,
            maximum = maximum,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global availability slot in status proposed. */
fun newGlobalAvailabilitySlot(code: String, name: String): GlobalAvailabilitySlot {
    return GlobalAvailabilitySlot(
        code = code,
        name = name,
        startsAt = 0,
        label = "",
        carrier = "",
        channel = "",
        rate = 0.0,
        revision = 0,
        maximum = 0.0,
        dock = "",
        status = GlobalAvailabilitySlotStatus.PROPOSED,
    )
}
