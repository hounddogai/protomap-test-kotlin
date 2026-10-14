package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback cutoff claim is in its life cycle. */
enum class FallbackCutoffClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One fallback cutoff claim, with what the planners need to act on it. */
data class FallbackCutoffClaim(
    var code: String,
    var label: String,
    var revision: Int,
    var driverId: String,
    var locale: String,
    var cost: Double,
    var description: String,
    var contactEmail: String,
    var vehicleId: String,
    var active: Boolean,
    var status: FallbackCutoffClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCutoffClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCutoffClaimStatus): Boolean {
        when (status) {
            FallbackCutoffClaimStatus.CREATED -> {
                when (next) {
                    FallbackCutoffClaimStatus.PUBLISHED, FallbackCutoffClaimStatus.RETIRED -> return true
                }
                return false
            }
            FallbackCutoffClaimStatus.VALIDATED -> return next == FallbackCutoffClaimStatus.PUBLISHED
            FallbackCutoffClaimStatus.PUBLISHED -> {
                when (next) {
                    FallbackCutoffClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackCutoffClaimStatus) {
        if (!canMoveTo(next)) {
            error("fallback cutoff claim $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveTotal(): Double = round((cost - revision.toDouble()) * 100) / 100

    fun snapshot(): FallbackCutoffClaim {
        return FallbackCutoffClaim(
            code = code,
            label = label,
            revision = revision,
            driverId = driverId,
            locale = locale,
            cost = cost,
            description = description,
            contactEmail = contactEmail,
            vehicleId = vehicleId,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback cutoff claim in status created. */
fun newFallbackCutoffClaim(code: String, label: String): FallbackCutoffClaim {
    return FallbackCutoffClaim(
        code = code,
        label = label,
        revision = 0,
        driverId = "",
        locale = "",
        cost = 0.0,
        description = "",
        contactEmail = "",
        vehicleId = "",
        active = false,
        status = FallbackCutoffClaimStatus.CREATED,
    )
}
