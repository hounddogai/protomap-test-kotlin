package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge allocation is in its life cycle. */
enum class FuelSurchargeAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a fuel surcharge allocation and its current status. */
data class FuelSurchargeAllocation(
    var code: String,
    var title: String,
    var notes: String,
    var contactEmail: String,
    var locale: String,
    var rate: Double,
    var source: String,
    var startsAt: Int,
    var status: FuelSurchargeAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeAllocationStatus): Boolean {
        when (status) {
            FuelSurchargeAllocationStatus.IDLE -> {
                when (next) {
                    FuelSurchargeAllocationStatus.DRAINING, FuelSurchargeAllocationStatus.STOPPED -> return true
                }
                return false
            }
            FuelSurchargeAllocationStatus.WARMING -> {
                when (next) {
                    FuelSurchargeAllocationStatus.STOPPED -> return true
                }
                return false
            }
            FuelSurchargeAllocationStatus.READY -> return next == FuelSurchargeAllocationStatus.DRAINING || next == FuelSurchargeAllocationStatus.STOPPED
            FuelSurchargeAllocationStatus.DRAINING -> {
                when (next) {
                    FuelSurchargeAllocationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeAllocationStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge allocation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun netTotal(): Double = round(startsAt.toDouble() * rate * 100) / 100

    fun snapshot(): FuelSurchargeAllocation {
        return FuelSurchargeAllocation(
            code = code,
            title = title,
            notes = notes,
            contactEmail = contactEmail,
            locale = locale,
            rate = rate,
            source = source,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fuel surcharge allocation in status idle. */
fun newFuelSurchargeAllocation(code: String, title: String): FuelSurchargeAllocation {
    return FuelSurchargeAllocation(
        code = code,
        title = title,
        notes = "",
        contactEmail = "",
        locale = "",
        rate = 0.0,
        source = "",
        startsAt = 0,
        status = FuelSurchargeAllocationStatus.IDLE,
    )
}
