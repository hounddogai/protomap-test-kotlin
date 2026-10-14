package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending tariff claim is in its life cycle. */
enum class PendingTariffClaimStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One pending tariff claim, with what the planners need to act on it. */
data class PendingTariffClaim(
    var code: String,
    var label: String,
    var vehicleId: String,
    var name: String,
    var pallets: Int,
    var ceiling: Double,
    var status: PendingTariffClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTariffClaimStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTariffClaimStatus): Boolean {
        when (status) {
            PendingTariffClaimStatus.PROPOSED -> {
                when (next) {
                    PendingTariffClaimStatus.APPROVED, PendingTariffClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingTariffClaimStatus.APPROVED -> {
                when (next) {
                    PendingTariffClaimStatus.REJECTED, PendingTariffClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingTariffClaimStatus.REJECTED -> return next == PendingTariffClaimStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PendingTariffClaimStatus) {
        if (!canMoveTo(next)) {
            error("pending tariff claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedTotal(): Double = round((pallets.toDouble() + ceiling) * 100) / 100

    fun snapshot(): PendingTariffClaim {
        return PendingTariffClaim(
            code = code,
            label = label,
            vehicleId = vehicleId,
            name = name,
            pallets = pallets,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending tariff claim in status proposed. */
fun newPendingTariffClaim(code: String, label: String): PendingTariffClaim {
    return PendingTariffClaim(
        code = code,
        label = label,
        vehicleId = "",
        name = "",
        pallets = 0,
        ceiling = 0.0,
        status = PendingTariffClaimStatus.PROPOSED,
    )
}
