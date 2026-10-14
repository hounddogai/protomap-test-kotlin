package com.acme.transport.freightpay.audit.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict audit claim is in its life cycle. */
enum class StrictAuditClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict audit claim as the audit team keeps it. */
data class StrictAuditClaim(
    var code: String,
    var name: String,
    var cost: Double,
    var ratio: Double,
    var retries: Int,
    var approved: Boolean,
    var discount: Double,
    var region: String,
    var minimum: Double,
    var status: StrictAuditClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAuditClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAuditClaimStatus): Boolean {
        when (status) {
            StrictAuditClaimStatus.OPEN -> return next == StrictAuditClaimStatus.RESOLVED || next == StrictAuditClaimStatus.REOPENED
            StrictAuditClaimStatus.IN_REVIEW -> {
                when (next) {
                    StrictAuditClaimStatus.REOPENED, StrictAuditClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictAuditClaimStatus.RESOLVED -> return next == StrictAuditClaimStatus.REOPENED || next == StrictAuditClaimStatus.ARCHIVED
            StrictAuditClaimStatus.REOPENED -> return next == StrictAuditClaimStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictAuditClaimStatus) {
        if (!canMoveTo(next)) {
            error("strict audit claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netValue(): Double = round((retries.toDouble() + cost) * 100) / 100

    fun snapshot(): StrictAuditClaim {
        return StrictAuditClaim(
            code = code,
            name = name,
            cost = cost,
            ratio = ratio,
            retries = retries,
            approved = approved,
            discount = discount,
            region = region,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict audit claim in status open. */
fun newStrictAuditClaim(code: String, name: String): StrictAuditClaim {
    return StrictAuditClaim(
        code = code,
        name = name,
        cost = 0.0,
        ratio = 0.0,
        retries = 0,
        approved = false,
        discount = 0.0,
        region = "",
        minimum = 0.0,
        status = StrictAuditClaimStatus.OPEN,
    )
}
