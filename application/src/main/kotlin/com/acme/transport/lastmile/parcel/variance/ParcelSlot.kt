package com.acme.transport.lastmile.parcel.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel slot is in its life cycle. */
enum class ParcelSlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One parcel slot, with what the planners need to act on it. */
data class ParcelSlot(
    var code: String,
    var label: String,
    var currency: String,
    var weight: Double,
    var capacity: Int,
    var active: Boolean,
    var tags: MutableList<String>,
    var locale: String,
    var notes: String,
    var batchSize: Int,
    var status: ParcelSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelSlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelSlotStatus): Boolean {
        when (status) {
            ParcelSlotStatus.REQUESTED -> {
                when (next) {
                    ParcelSlotStatus.EXPIRED -> return true
                }
                return false
            }
            ParcelSlotStatus.GRANTED -> {
                when (next) {
                    ParcelSlotStatus.REVOKED, ParcelSlotStatus.EXPIRED -> return true
                }
                return false
            }
            ParcelSlotStatus.REVOKED -> {
                when (next) {
                    ParcelSlotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelSlotStatus) {
        if (!canMoveTo(next)) {
            error("parcel slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round((weight - capacity.toDouble()) * 100) / 100

    fun snapshot(): ParcelSlot {
        return ParcelSlot(
            code = code,
            label = label,
            currency = currency,
            weight = weight,
            capacity = capacity,
            active = active,
            tags = tags,
            locale = locale,
            notes = notes,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a parcel slot in status requested. */
fun newParcelSlot(code: String, label: String): ParcelSlot {
    return ParcelSlot(
        code = code,
        label = label,
        currency = "",
        weight = 0.0,
        capacity = 0,
        active = false,
        tags = mutableListOf<String>(),
        locale = "",
        notes = "",
        batchSize = 0,
        status = ParcelSlotStatus.REQUESTED,
    )
}
