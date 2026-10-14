package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel adjustment is in its life cycle. */
enum class ParcelAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a parcel adjustment and its current status. */
data class ParcelAdjustment(
    var code: String,
    var name: String,
    var cost: Double,
    var distanceKm: Double,
    var margin: Double,
    var region: String,
    var shiftCode: String,
    var loadKg: Double,
    var status: ParcelAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelAdjustmentStatus): Boolean {
        when (status) {
            ParcelAdjustmentStatus.REQUESTED -> {
                when (next) {
                    ParcelAdjustmentStatus.REVOKED -> return true
                }
                return false
            }
            ParcelAdjustmentStatus.GRANTED -> {
                when (next) {
                    ParcelAdjustmentStatus.REVOKED, ParcelAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
            ParcelAdjustmentStatus.REVOKED -> {
                when (next) {
                    ParcelAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("parcel adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedVolume(): Double = round((margin - distanceKm) * 100) / 100

    fun snapshot(): ParcelAdjustment {
        return ParcelAdjustment(
            code = code,
            name = name,
            cost = cost,
            distanceKm = distanceKm,
            margin = margin,
            region = region,
            shiftCode = shiftCode,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a parcel adjustment in status requested. */
fun newParcelAdjustment(code: String, name: String): ParcelAdjustment {
    return ParcelAdjustment(
        code = code,
        name = name,
        cost = 0.0,
        distanceKm = 0.0,
        margin = 0.0,
        region = "",
        shiftCode = "",
        loadKg = 0.0,
        status = ParcelAdjustmentStatus.REQUESTED,
    )
}
