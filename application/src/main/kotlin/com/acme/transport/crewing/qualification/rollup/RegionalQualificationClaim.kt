package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional qualification claim is in its life cycle. */
enum class RegionalQualificationClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One regional qualification claim, with what the planners need to act on it. */
data class RegionalQualificationClaim(
    var code: String,
    var name: String,
    var quantity: Int,
    var distanceKm: Double,
    var phone: String,
    var status: RegionalQualificationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalQualificationClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalQualificationClaimStatus): Boolean {
        when (status) {
            RegionalQualificationClaimStatus.REQUESTED -> {
                when (next) {
                    RegionalQualificationClaimStatus.REVOKED -> return true
                }
                return false
            }
            RegionalQualificationClaimStatus.GRANTED -> return next == RegionalQualificationClaimStatus.EXPIRED
            RegionalQualificationClaimStatus.REVOKED -> {
                when (next) {
                    RegionalQualificationClaimStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalQualificationClaimStatus) {
        if (!canMoveTo(next)) {
            error("regional qualification claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netExposure(): Double = round((quantity.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): RegionalQualificationClaim {
        return RegionalQualificationClaim(
            code = code,
            name = name,
            quantity = quantity,
            distanceKm = distanceKm,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional qualification claim in status requested. */
fun newRegionalQualificationClaim(code: String, name: String): RegionalQualificationClaim {
    return RegionalQualificationClaim(
        code = code,
        name = name,
        quantity = 0,
        distanceKm = 0.0,
        phone = "",
        status = RegionalQualificationClaimStatus.REQUESTED,
    )
}
