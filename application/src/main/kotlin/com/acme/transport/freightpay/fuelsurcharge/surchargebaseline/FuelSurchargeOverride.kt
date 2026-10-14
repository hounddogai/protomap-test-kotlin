package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge override is in its life cycle. */
enum class FuelSurchargeOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a fuel surcharge override and its current status. */
data class FuelSurchargeOverride(
    var code: String,
    var title: String,
    var segment: String,
    var unit: String,
    var startsAt: Int,
    var capacity: Int,
    var rate: Double,
    var cost: Double,
    var reviewer: String,
    var archived: Boolean,
    var limit: Int,
    var status: FuelSurchargeOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeOverrideStatus): Boolean {
        when (status) {
            FuelSurchargeOverrideStatus.DRAFT -> return next == FuelSurchargeOverrideStatus.SUSPENDED
            FuelSurchargeOverrideStatus.PENDING -> return next == FuelSurchargeOverrideStatus.CLOSED
            FuelSurchargeOverrideStatus.ACTIVE -> return next == FuelSurchargeOverrideStatus.SUSPENDED || next == FuelSurchargeOverrideStatus.CLOSED
            FuelSurchargeOverrideStatus.SUSPENDED -> {
                when (next) {
                    FuelSurchargeOverrideStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeOverrideStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedVolume(): Double = round((startsAt.toDouble() + rate) * 100) / 100

    fun snapshot(): FuelSurchargeOverride {
        return FuelSurchargeOverride(
            code = code,
            title = title,
            segment = segment,
            unit = unit,
            startsAt = startsAt,
            capacity = capacity,
            rate = rate,
            cost = cost,
            reviewer = reviewer,
            archived = archived,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fuel surcharge override in status draft. */
fun newFuelSurchargeOverride(code: String, title: String): FuelSurchargeOverride {
    return FuelSurchargeOverride(
        code = code,
        title = title,
        segment = "",
        unit = "",
        startsAt = 0,
        capacity = 0,
        rate = 0.0,
        cost = 0.0,
        reviewer = "",
        archived = false,
        limit = 0,
        status = FuelSurchargeOverrideStatus.DRAFT,
    )
}
