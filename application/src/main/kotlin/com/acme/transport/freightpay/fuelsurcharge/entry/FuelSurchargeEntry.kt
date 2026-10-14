package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel surcharge entry records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a fuel surcharge entry is in its life cycle. */
enum class FuelSurchargeEntryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fuel surcharge entry. Its code is unique within a tenant. */
data class FuelSurchargeEntry(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var retries: Int,
    var currency: String,
    var pinned: Boolean,
    var floorValue: Double,
    var threshold: Double,
    var status: FuelSurchargeEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeEntryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeEntryStatus): Boolean {
        when (status) {
            FuelSurchargeEntryStatus.PLANNED -> {
                when (next) {
                    FuelSurchargeEntryStatus.IN_PROGRESS, FuelSurchargeEntryStatus.CANCELLED -> return true
                }
                return false
            }
            FuelSurchargeEntryStatus.SCHEDULED -> {
                when (next) {
                    FuelSurchargeEntryStatus.COMPLETED -> return true
                }
                return false
            }
            FuelSurchargeEntryStatus.IN_PROGRESS -> {
                when (next) {
                    FuelSurchargeEntryStatus.CANCELLED -> return true
                }
                return false
            }
            FuelSurchargeEntryStatus.COMPLETED -> {
                when (next) {
                    FuelSurchargeEntryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeEntryStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveExposure(): Double = round((distanceKm - floorValue) * 100) / 100

    fun snapshot(): FuelSurchargeEntry {
        return FuelSurchargeEntry(
            code = code,
            title = title,
            distanceKm = distanceKm,
            retries = retries,
            currency = currency,
            pinned = pinned,
            floorValue = floorValue,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel surcharge entry in status planned. */
fun newFuelSurchargeEntry(code: String, title: String): FuelSurchargeEntry {
    return FuelSurchargeEntry(
        code = code,
        title = title,
        distanceKm = 0.0,
        retries = 0,
        currency = "",
        pinned = false,
        floorValue = 0.0,
        threshold = 0.0,
        status = FuelSurchargeEntryStatus.PLANNED,
    )
}
