package com.acme.transport.safety.incident.globalincidentproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary incident claim is in its life cycle. */
enum class PrimaryIncidentClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a primary incident claim and its current status. */
data class PrimaryIncidentClaim(
    var code: String,
    var title: String,
    var country: String,
    var rate: Double,
    var approved: Boolean,
    var limit: Int,
    var segment: String,
    var region: String,
    var depot: String,
    var status: PrimaryIncidentClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryIncidentClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryIncidentClaimStatus): Boolean {
        when (status) {
            PrimaryIncidentClaimStatus.OPEN -> return next == PrimaryIncidentClaimStatus.IN_REVIEW || next == PrimaryIncidentClaimStatus.REOPENED
            PrimaryIncidentClaimStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryIncidentClaimStatus.REOPENED, PrimaryIncidentClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryIncidentClaimStatus.RESOLVED -> {
                when (next) {
                    PrimaryIncidentClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryIncidentClaimStatus.REOPENED -> {
                when (next) {
                    PrimaryIncidentClaimStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryIncidentClaimStatus) {
        if (!canMoveTo(next)) {
            error("primary incident claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round((limit.toDouble() + rate) * 100) / 100

    fun snapshot(): PrimaryIncidentClaim {
        return PrimaryIncidentClaim(
            code = code,
            title = title,
            country = country,
            rate = rate,
            approved = approved,
            limit = limit,
            segment = segment,
            region = region,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary incident claim in status open. */
fun newPrimaryIncidentClaim(code: String, title: String): PrimaryIncidentClaim {
    return PrimaryIncidentClaim(
        code = code,
        title = title,
        country = "",
        rate = 0.0,
        approved = false,
        limit = 0,
        segment = "",
        region = "",
        depot = "",
        status = PrimaryIncidentClaimStatus.OPEN,
    )
}
