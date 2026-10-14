package com.acme.transport.lastmile.parcel.tieredparcelvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual parcel claim is in its life cycle. */
enum class ManualParcelClaimStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual parcel claim. Its code is unique within a tenant. */
data class ManualParcelClaim(
    var code: String,
    var name: String,
    var retries: Int,
    var trailerId: String,
    var active: Boolean,
    var vehicleId: String,
    var segment: String,
    var region: String,
    var fuelLiters: Double,
    var sequence: Int,
    var startsAt: Int,
    var status: ManualParcelClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualParcelClaimStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualParcelClaimStatus): Boolean {
        when (status) {
            ManualParcelClaimStatus.PROPOSED -> {
                when (next) {
                    ManualParcelClaimStatus.APPROVED, ManualParcelClaimStatus.REJECTED -> return true
                }
                return false
            }
            ManualParcelClaimStatus.APPROVED -> return next == ManualParcelClaimStatus.REJECTED || next == ManualParcelClaimStatus.WITHDRAWN
            ManualParcelClaimStatus.REJECTED -> return next == ManualParcelClaimStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualParcelClaimStatus) {
        if (!canMoveTo(next)) {
            error("manual parcel claim $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netLoad(): Double = round((sequence.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): ManualParcelClaim {
        return ManualParcelClaim(
            code = code,
            name = name,
            retries = retries,
            trailerId = trailerId,
            active = active,
            vehicleId = vehicleId,
            segment = segment,
            region = region,
            fuelLiters = fuelLiters,
            sequence = sequence,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a manual parcel claim in status proposed. */
fun newManualParcelClaim(code: String, name: String): ManualParcelClaim {
    return ManualParcelClaim(
        code = code,
        name = name,
        retries = 0,
        trailerId = "",
        active = false,
        vehicleId = "",
        segment = "",
        region = "",
        fuelLiters = 0.0,
        sequence = 0,
        startsAt = 0,
        status = ManualParcelClaimStatus.PROPOSED,
    )
}
