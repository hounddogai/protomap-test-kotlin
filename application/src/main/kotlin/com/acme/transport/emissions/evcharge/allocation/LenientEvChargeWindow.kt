package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.kit.text.normalize

/** Where a lenient ev charge window is in its life cycle. */
enum class LenientEvChargeWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient ev charge window. Its code is unique within a tenant. */
data class LenientEvChargeWindow(
    var code: String,
    var name: String,
    var contactEmail: String,
    var quantity: Int,
    var vehicleId: String,
    var status: LenientEvChargeWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientEvChargeWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientEvChargeWindowStatus): Boolean {
        when (status) {
            LenientEvChargeWindowStatus.CREATED -> return next == LenientEvChargeWindowStatus.PUBLISHED || next == LenientEvChargeWindowStatus.RETIRED
            LenientEvChargeWindowStatus.VALIDATED -> {
                when (next) {
                    LenientEvChargeWindowStatus.PUBLISHED -> return true
                }
                return false
            }
            LenientEvChargeWindowStatus.PUBLISHED -> return next == LenientEvChargeWindowStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientEvChargeWindowStatus) {
        if (!canMoveTo(next)) {
            error("lenient ev charge window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): LenientEvChargeWindow {
        return LenientEvChargeWindow(
            code = code,
            name = name,
            contactEmail = contactEmail,
            quantity = quantity,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient ev charge window in status created. */
fun newLenientEvChargeWindow(code: String, name: String): LenientEvChargeWindow {
    return LenientEvChargeWindow(
        code = code,
        name = name,
        contactEmail = "",
        quantity = 0,
        vehicleId = "",
        status = LenientEvChargeWindowStatus.CREATED,
    )
}
