package com.acme.transport.emissions.evcharge.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled ev charge slot is in its life cycle. */
enum class ScheduledEvChargeSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One scheduled ev charge slot, with what the planners need to act on it. */
data class ScheduledEvChargeSlot(
    var code: String,
    var label: String,
    var score: Double,
    var pallets: Int,
    var floorValue: Double,
    var attributes: MutableMap<String, String>,
    var source: String,
    var externalRef: String,
    var attempts: Int,
    var status: ScheduledEvChargeSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledEvChargeSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledEvChargeSlotStatus): Boolean {
        when (status) {
            ScheduledEvChargeSlotStatus.OPEN -> {
                when (next) {
                    ScheduledEvChargeSlotStatus.RESOLVED -> return true
                }
                return false
            }
            ScheduledEvChargeSlotStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledEvChargeSlotStatus.RESOLVED, ScheduledEvChargeSlotStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledEvChargeSlotStatus.RESOLVED -> return next == ScheduledEvChargeSlotStatus.REOPENED || next == ScheduledEvChargeSlotStatus.ARCHIVED
            ScheduledEvChargeSlotStatus.REOPENED -> {
                when (next) {
                    ScheduledEvChargeSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledEvChargeSlotStatus) {
        if (!canMoveTo(next)) {
            error("scheduled ev charge slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled ev charge slot"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round(floorValue * pallets.toDouble() * 100) / 100

    fun snapshot(): ScheduledEvChargeSlot {
        return ScheduledEvChargeSlot(
            code = code,
            label = label,
            score = score,
            pallets = pallets,
            floorValue = floorValue,
            attributes = attributes,
            source = source,
            externalRef = externalRef,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a scheduled ev charge slot in status open. */
fun newScheduledEvChargeSlot(code: String, label: String): ScheduledEvChargeSlot {
    return ScheduledEvChargeSlot(
        code = code,
        label = label,
        score = 0.0,
        pallets = 0,
        floorValue = 0.0,
        attributes = mutableMapOf<String, String>(),
        source = "",
        externalRef = "",
        attempts = 0,
        status = ScheduledEvChargeSlotStatus.OPEN,
    )
}
