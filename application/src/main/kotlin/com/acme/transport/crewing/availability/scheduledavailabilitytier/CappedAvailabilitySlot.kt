package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped availability slot is in its life cycle. */
enum class CappedAvailabilitySlotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A capped availability slot as the availability team keeps it. */
data class CappedAvailabilitySlot(
    var code: String,
    var title: String,
    var dock: String,
    var durationMinutes: Int,
    var tags: MutableList<String>,
    var weight: Double,
    var approved: Boolean,
    var archived: Boolean,
    var margin: Double,
    var channel: String,
    var stopCount: Int,
    var status: CappedAvailabilitySlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAvailabilitySlotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAvailabilitySlotStatus): Boolean {
        when (status) {
            CappedAvailabilitySlotStatus.IDLE -> return next == CappedAvailabilitySlotStatus.WARMING || next == CappedAvailabilitySlotStatus.DRAINING
            CappedAvailabilitySlotStatus.WARMING -> return next == CappedAvailabilitySlotStatus.STOPPED
            CappedAvailabilitySlotStatus.READY -> {
                when (next) {
                    CappedAvailabilitySlotStatus.DRAINING, CappedAvailabilitySlotStatus.STOPPED -> return true
                }
                return false
            }
            CappedAvailabilitySlotStatus.DRAINING -> return next == CappedAvailabilitySlotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CappedAvailabilitySlotStatus) {
        if (!canMoveTo(next)) {
            error("capped availability slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedVolume(): Double = round((stopCount.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): CappedAvailabilitySlot {
        return CappedAvailabilitySlot(
            code = code,
            title = title,
            dock = dock,
            durationMinutes = durationMinutes,
            tags = tags,
            weight = weight,
            approved = approved,
            archived = archived,
            margin = margin,
            channel = channel,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capped availability slot in status idle. */
fun newCappedAvailabilitySlot(code: String, title: String): CappedAvailabilitySlot {
    return CappedAvailabilitySlot(
        code = code,
        title = title,
        dock = "",
        durationMinutes = 0,
        tags = mutableListOf<String>(),
        weight = 0.0,
        approved = false,
        archived = false,
        margin = 0.0,
        channel = "",
        stopCount = 0,
        status = CappedAvailabilitySlotStatus.IDLE,
    )
}
