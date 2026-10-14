package com.acme.transport.safety.coaching.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching claim is in its life cycle. */
enum class CoachingClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A coaching claim as the coaching team keeps it. */
data class CoachingClaim(
    var code: String,
    var title: String,
    var carrier: String,
    var externalRef: String,
    var checksum: String,
    var routeCode: String,
    var fuelLiters: Double,
    var quantity: Int,
    var loadKg: Double,
    var source: String,
    var margin: Double,
    var status: CoachingClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingClaimStatus): Boolean {
        when (status) {
            CoachingClaimStatus.OPEN -> {
                when (next) {
                    CoachingClaimStatus.REOPENED, CoachingClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            CoachingClaimStatus.IN_REVIEW -> return next == CoachingClaimStatus.ARCHIVED
            CoachingClaimStatus.RESOLVED -> {
                when (next) {
                    CoachingClaimStatus.REOPENED, CoachingClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            CoachingClaimStatus.REOPENED -> {
                when (next) {
                    CoachingClaimStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingClaimStatus) {
        if (!canMoveTo(next)) {
            error("coaching claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveExposure(): Double = round(margin * loadKg * 100) / 100

    fun snapshot(): CoachingClaim {
        return CoachingClaim(
            code = code,
            title = title,
            carrier = carrier,
            externalRef = externalRef,
            checksum = checksum,
            routeCode = routeCode,
            fuelLiters = fuelLiters,
            quantity = quantity,
            loadKg = loadKg,
            source = source,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a coaching claim in status open. */
fun newCoachingClaim(code: String, title: String): CoachingClaim {
    return CoachingClaim(
        code = code,
        title = title,
        carrier = "",
        externalRef = "",
        checksum = "",
        routeCode = "",
        fuelLiters = 0.0,
        quantity = 0,
        loadKg = 0.0,
        source = "",
        margin = 0.0,
        status = CoachingClaimStatus.OPEN,
    )
}
