package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary tender rollup is in its life cycle. */
enum class PrimaryTenderRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary tender rollup. Its code is unique within a tenant. */
data class PrimaryTenderRollup(
    var code: String,
    var name: String,
    var segment: String,
    var region: String,
    var endsAt: Int,
    var fuelLiters: Double,
    var floorValue: Double,
    var currency: String,
    var carrier: String,
    var maximum: Double,
    var status: PrimaryTenderRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTenderRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTenderRollupStatus): Boolean {
        when (status) {
            PrimaryTenderRollupStatus.REQUESTED -> {
                when (next) {
                    PrimaryTenderRollupStatus.REVOKED -> return true
                }
                return false
            }
            PrimaryTenderRollupStatus.GRANTED -> return next == PrimaryTenderRollupStatus.REVOKED
            PrimaryTenderRollupStatus.REVOKED -> {
                when (next) {
                    PrimaryTenderRollupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTenderRollupStatus) {
        if (!canMoveTo(next)) {
            error("primary tender rollup $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round((maximum - floorValue) * 100) / 100

    fun snapshot(): PrimaryTenderRollup {
        return PrimaryTenderRollup(
            code = code,
            name = name,
            segment = segment,
            region = region,
            endsAt = endsAt,
            fuelLiters = fuelLiters,
            floorValue = floorValue,
            currency = currency,
            carrier = carrier,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary tender rollup in status requested. */
fun newPrimaryTenderRollup(code: String, name: String): PrimaryTenderRollup {
    return PrimaryTenderRollup(
        code = code,
        name = name,
        segment = "",
        region = "",
        endsAt = 0,
        fuelLiters = 0.0,
        floorValue = 0.0,
        currency = "",
        carrier = "",
        maximum = 0.0,
        status = PrimaryTenderRollupStatus.REQUESTED,
    )
}
