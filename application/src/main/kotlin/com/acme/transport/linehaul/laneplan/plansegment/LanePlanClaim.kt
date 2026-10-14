package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.text.normalize

/** Where a lane plan claim is in its life cycle. */
enum class LanePlanClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a lane plan claim and its current status. */
data class LanePlanClaim(
    var code: String,
    var title: String,
    var depot: String,
    var active: Boolean,
    var country: String,
    var distanceKm: Double,
    var externalRef: String,
    var source: String,
    var status: LanePlanClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanClaimStatus): Boolean {
        when (status) {
            LanePlanClaimStatus.OPEN -> return next == LanePlanClaimStatus.IN_REVIEW
            LanePlanClaimStatus.IN_REVIEW -> {
                when (next) {
                    LanePlanClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            LanePlanClaimStatus.RESOLVED -> {
                when (next) {
                    LanePlanClaimStatus.REOPENED, LanePlanClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            LanePlanClaimStatus.REOPENED -> {
                when (next) {
                    LanePlanClaimStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanClaimStatus) {
        if (!canMoveTo(next)) {
            error("lane plan claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): LanePlanClaim {
        return LanePlanClaim(
            code = code,
            title = title,
            depot = depot,
            active = active,
            country = country,
            distanceKm = distanceKm,
            externalRef = externalRef,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lane plan claim in status open. */
fun newLanePlanClaim(code: String, title: String): LanePlanClaim {
    return LanePlanClaim(
        code = code,
        title = title,
        depot = "",
        active = false,
        country = "",
        distanceKm = 0.0,
        externalRef = "",
        source = "",
        status = LanePlanClaimStatus.OPEN,
    )
}
