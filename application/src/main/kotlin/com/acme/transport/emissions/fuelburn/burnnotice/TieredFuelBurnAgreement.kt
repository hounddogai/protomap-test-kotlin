package com.acme.transport.emissions.fuelburn.burnnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered fuel burn agreement is in its life cycle. */
enum class TieredFuelBurnAgreementStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tiered fuel burn agreement as the fuel burn team keeps it. */
data class TieredFuelBurnAgreement(
    var code: String,
    var name: String,
    var driverId: String,
    var vehicleId: String,
    var loadKg: Double,
    var visible: Boolean,
    var maximum: Double,
    var segment: String,
    var durationMinutes: Int,
    var status: TieredFuelBurnAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFuelBurnAgreementStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFuelBurnAgreementStatus): Boolean {
        when (status) {
            TieredFuelBurnAgreementStatus.REQUESTED -> return next == TieredFuelBurnAgreementStatus.REVOKED || next == TieredFuelBurnAgreementStatus.EXPIRED
            TieredFuelBurnAgreementStatus.GRANTED -> {
                when (next) {
                    TieredFuelBurnAgreementStatus.REVOKED -> return true
                }
                return false
            }
            TieredFuelBurnAgreementStatus.REVOKED -> return next == TieredFuelBurnAgreementStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TieredFuelBurnAgreementStatus) {
        if (!canMoveTo(next)) {
            error("tiered fuel burn agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveLoad(): Double = round((loadKg + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): TieredFuelBurnAgreement {
        return TieredFuelBurnAgreement(
            code = code,
            name = name,
            driverId = driverId,
            vehicleId = vehicleId,
            loadKg = loadKg,
            visible = visible,
            maximum = maximum,
            segment = segment,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered fuel burn agreement in status requested. */
fun newTieredFuelBurnAgreement(code: String, name: String): TieredFuelBurnAgreement {
    return TieredFuelBurnAgreement(
        code = code,
        name = name,
        driverId = "",
        vehicleId = "",
        loadKg = 0.0,
        visible = false,
        maximum = 0.0,
        segment = "",
        durationMinutes = 0,
        status = TieredFuelBurnAgreementStatus.REQUESTED,
    )
}
