package com.acme.transport.lastmile.redelivery.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery slot is in its life cycle. */
enum class RedeliverySlotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One redelivery slot, with what the planners need to act on it. */
data class RedeliverySlot(
    var code: String,
    var label: String,
    var name: String,
    var stopCount: Int,
    var phone: String,
    var unit: String,
    var source: String,
    var minimum: Double,
    var depot: String,
    var shiftCode: String,
    var status: RedeliverySlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliverySlotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliverySlotStatus): Boolean {
        when (status) {
            RedeliverySlotStatus.CREATED -> {
                when (next) {
                    RedeliverySlotStatus.PUBLISHED -> return true
                }
                return false
            }
            RedeliverySlotStatus.VALIDATED -> {
                when (next) {
                    RedeliverySlotStatus.PUBLISHED -> return true
                }
                return false
            }
            RedeliverySlotStatus.PUBLISHED -> return next == RedeliverySlotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RedeliverySlotStatus) {
        if (!canMoveTo(next)) {
            error("redelivery slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round((stopCount.toDouble() - minimum) * 100) / 100

    fun snapshot(): RedeliverySlot {
        return RedeliverySlot(
            code = code,
            label = label,
            name = name,
            stopCount = stopCount,
            phone = phone,
            unit = unit,
            source = source,
            minimum = minimum,
            depot = depot,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a redelivery slot in status created. */
fun newRedeliverySlot(code: String, label: String): RedeliverySlot {
    return RedeliverySlot(
        code = code,
        label = label,
        name = "",
        stopCount = 0,
        phone = "",
        unit = "",
        source = "",
        minimum = 0.0,
        depot = "",
        shiftCode = "",
        status = RedeliverySlotStatus.CREATED,
    )
}
