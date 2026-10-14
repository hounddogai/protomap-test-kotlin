package com.acme.transport.customs.tariff.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tariff claim records of customs's tariff work, and the rules, stores, and calculations around them.

/** Where a tariff claim is in its life cycle. */
enum class TariffClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tariff claim. Its code is unique within a tenant. */
data class TariffClaim(
    var code: String,
    var label: String,
    var floorValue: Double,
    var loadKg: Double,
    var notes: String,
    var ownerId: String,
    var discount: Double,
    var visible: Boolean,
    var driverId: String,
    var status: TariffClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffClaimStatus): Boolean {
        when (status) {
            TariffClaimStatus.PLANNED -> return next == TariffClaimStatus.IN_PROGRESS
            TariffClaimStatus.SCHEDULED -> return next == TariffClaimStatus.IN_PROGRESS || next == TariffClaimStatus.COMPLETED
            TariffClaimStatus.IN_PROGRESS -> {
                when (next) {
                    TariffClaimStatus.COMPLETED, TariffClaimStatus.CANCELLED -> return true
                }
                return false
            }
            TariffClaimStatus.COMPLETED -> return next == TariffClaimStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TariffClaimStatus) {
        if (!canMoveTo(next)) {
            error("tariff claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedVolume(): Double = round((loadKg + discount) * 100) / 100

    fun snapshot(): TariffClaim {
        return TariffClaim(
            code = code,
            label = label,
            floorValue = floorValue,
            loadKg = loadKg,
            notes = notes,
            ownerId = ownerId,
            discount = discount,
            visible = visible,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tariff claim in status planned. */
fun newTariffClaim(code: String, label: String): TariffClaim {
    return TariffClaim(
        code = code,
        label = label,
        floorValue = 0.0,
        loadKg = 0.0,
        notes = "",
        ownerId = "",
        discount = 0.0,
        visible = false,
        driverId = "",
        status = TariffClaimStatus.PLANNED,
    )
}
