package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul claim is in its life cycle. */
enum class BackhaulClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a backhaul claim and its current status. */
data class BackhaulClaim(
    var code: String,
    var name: String,
    var vehicleId: String,
    var description: String,
    var retries: Int,
    var etaMinutes: Int,
    var status: BackhaulClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulClaimStatus): Boolean {
        when (status) {
            BackhaulClaimStatus.CREATED -> return next == BackhaulClaimStatus.VALIDATED || next == BackhaulClaimStatus.RETIRED
            BackhaulClaimStatus.VALIDATED -> return next == BackhaulClaimStatus.RETIRED
            BackhaulClaimStatus.PUBLISHED -> return next == BackhaulClaimStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BackhaulClaimStatus) {
        if (!canMoveTo(next)) {
            error("backhaul claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedLoad(): Double = round((retries.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): BackhaulClaim {
        return BackhaulClaim(
            code = code,
            name = name,
            vehicleId = vehicleId,
            description = description,
            retries = retries,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a backhaul claim in status created. */
fun newBackhaulClaim(code: String, name: String): BackhaulClaim {
    return BackhaulClaim(
        code = code,
        name = name,
        vehicleId = "",
        description = "",
        retries = 0,
        etaMinutes = 0,
        status = BackhaulClaimStatus.CREATED,
    )
}
