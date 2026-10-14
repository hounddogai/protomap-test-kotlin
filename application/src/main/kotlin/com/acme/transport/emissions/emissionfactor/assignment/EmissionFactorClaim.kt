package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.kit.text.normalize

/** Where a emission factor claim is in its life cycle. */
enum class EmissionFactorClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a emission factor claim and its current status. */
data class EmissionFactorClaim(
    var code: String,
    var name: String,
    var endsAt: Int,
    var visible: Boolean,
    var active: Boolean,
    var shiftCode: String,
    var timezone: String,
    var region: String,
    var segment: String,
    var vehicleId: String,
    var status: EmissionFactorClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorClaimStatus): Boolean {
        when (status) {
            EmissionFactorClaimStatus.IDLE -> {
                when (next) {
                    EmissionFactorClaimStatus.WARMING, EmissionFactorClaimStatus.DRAINING -> return true
                }
                return false
            }
            EmissionFactorClaimStatus.WARMING -> {
                when (next) {
                    EmissionFactorClaimStatus.READY, EmissionFactorClaimStatus.STOPPED -> return true
                }
                return false
            }
            EmissionFactorClaimStatus.READY -> {
                when (next) {
                    EmissionFactorClaimStatus.STOPPED -> return true
                }
                return false
            }
            EmissionFactorClaimStatus.DRAINING -> return next == EmissionFactorClaimStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: EmissionFactorClaimStatus) {
        if (!canMoveTo(next)) {
            error("emission factor claim $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): EmissionFactorClaim {
        return EmissionFactorClaim(
            code = code,
            name = name,
            endsAt = endsAt,
            visible = visible,
            active = active,
            shiftCode = shiftCode,
            timezone = timezone,
            region = region,
            segment = segment,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a emission factor claim in status idle. */
fun newEmissionFactorClaim(code: String, name: String): EmissionFactorClaim {
    return EmissionFactorClaim(
        code = code,
        name = name,
        endsAt = 0,
        visible = false,
        active = false,
        shiftCode = "",
        timezone = "",
        region = "",
        segment = "",
        vehicleId = "",
        status = EmissionFactorClaimStatus.IDLE,
    )
}
