package com.acme.transport.emissions.evcharge.regionalchargeslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default ev charge slot records of emissions's ev charge work, and the rules, stores, and calculations
// around them.

/** Where a default ev charge slot is in its life cycle. */
enum class DefaultEvChargeSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a default ev charge slot and its current status. */
data class DefaultEvChargeSlot(
    var code: String,
    var title: String,
    var revision: Int,
    var version: Int,
    var retries: Int,
    var cost: Double,
    var ceiling: Double,
    var notes: String,
    var rate: Double,
    var archived: Boolean,
    var distanceKm: Double,
    var status: DefaultEvChargeSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultEvChargeSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultEvChargeSlotStatus): Boolean {
        when (status) {
            DefaultEvChargeSlotStatus.PLANNED -> return next == DefaultEvChargeSlotStatus.SCHEDULED || next == DefaultEvChargeSlotStatus.IN_PROGRESS
            DefaultEvChargeSlotStatus.SCHEDULED -> {
                when (next) {
                    DefaultEvChargeSlotStatus.IN_PROGRESS, DefaultEvChargeSlotStatus.COMPLETED -> return true
                }
                return false
            }
            DefaultEvChargeSlotStatus.IN_PROGRESS -> return next == DefaultEvChargeSlotStatus.COMPLETED
            DefaultEvChargeSlotStatus.COMPLETED -> return next == DefaultEvChargeSlotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DefaultEvChargeSlotStatus) {
        if (!canMoveTo(next)) {
            error("default ev charge slot $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round(revision.toDouble() * rate * 100) / 100

    fun snapshot(): DefaultEvChargeSlot {
        return DefaultEvChargeSlot(
            code = code,
            title = title,
            revision = revision,
            version = version,
            retries = retries,
            cost = cost,
            ceiling = ceiling,
            notes = notes,
            rate = rate,
            archived = archived,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default ev charge slot in status planned. */
fun newDefaultEvChargeSlot(code: String, title: String): DefaultEvChargeSlot {
    return DefaultEvChargeSlot(
        code = code,
        title = title,
        revision = 0,
        version = 0,
        retries = 0,
        cost = 0.0,
        ceiling = 0.0,
        notes = "",
        rate = 0.0,
        archived = false,
        distanceKm = 0.0,
        status = DefaultEvChargeSlotStatus.PLANNED,
    )
}
