package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary defect claim is in its life cycle. */
enum class PrimaryDefectClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary defect claim as the defect team keeps it. */
data class PrimaryDefectClaim(
    var code: String,
    var title: String,
    var archived: Boolean,
    var attempts: Int,
    var fuelLiters: Double,
    var status: PrimaryDefectClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDefectClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDefectClaimStatus): Boolean {
        when (status) {
            PrimaryDefectClaimStatus.CREATED -> return next == PrimaryDefectClaimStatus.PUBLISHED
            PrimaryDefectClaimStatus.VALIDATED -> return next == PrimaryDefectClaimStatus.PUBLISHED || next == PrimaryDefectClaimStatus.RETIRED
            PrimaryDefectClaimStatus.PUBLISHED -> {
                when (next) {
                    PrimaryDefectClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryDefectClaimStatus) {
        if (!canMoveTo(next)) {
            error("primary defect claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary defect claim"} $code"

    fun netVolume(): Double = round((fuelLiters - attempts.toDouble()) * 100) / 100

    fun snapshot(): PrimaryDefectClaim {
        return PrimaryDefectClaim(
            code = code,
            title = title,
            archived = archived,
            attempts = attempts,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary defect claim in status created. */
fun newPrimaryDefectClaim(code: String, title: String): PrimaryDefectClaim {
    return PrimaryDefectClaim(
        code = code,
        title = title,
        archived = false,
        attempts = 0,
        fuelLiters = 0.0,
        status = PrimaryDefectClaimStatus.CREATED,
    )
}
