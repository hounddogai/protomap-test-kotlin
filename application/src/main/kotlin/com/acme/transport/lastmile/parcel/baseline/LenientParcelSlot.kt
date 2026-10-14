package com.acme.transport.lastmile.parcel.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient parcel slot is in its life cycle. */
enum class LenientParcelSlotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient parcel slot as the parcel team keeps it. */
data class LenientParcelSlot(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var source: String,
    var weight: Double,
    var notes: String,
    var region: String,
    var externalRef: String,
    var odometer: Int,
    var locale: String,
    var status: LenientParcelSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientParcelSlotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientParcelSlotStatus): Boolean {
        when (status) {
            LenientParcelSlotStatus.CREATED -> {
                when (next) {
                    LenientParcelSlotStatus.VALIDATED -> return true
                }
                return false
            }
            LenientParcelSlotStatus.VALIDATED -> return next == LenientParcelSlotStatus.RETIRED
            LenientParcelSlotStatus.PUBLISHED -> {
                when (next) {
                    LenientParcelSlotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientParcelSlotStatus) {
        if (!canMoveTo(next)) {
            error("lenient parcel slot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round((updatedAt.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): LenientParcelSlot {
        return LenientParcelSlot(
            code = code,
            name = name,
            updatedAt = updatedAt,
            source = source,
            weight = weight,
            notes = notes,
            region = region,
            externalRef = externalRef,
            odometer = odometer,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lenient parcel slot in status created. */
fun newLenientParcelSlot(code: String, name: String): LenientParcelSlot {
    return LenientParcelSlot(
        code = code,
        name = name,
        updatedAt = 0,
        source = "",
        weight = 0.0,
        notes = "",
        region = "",
        externalRef = "",
        odometer = 0,
        locale = "",
        status = LenientParcelSlotStatus.CREATED,
    )
}
