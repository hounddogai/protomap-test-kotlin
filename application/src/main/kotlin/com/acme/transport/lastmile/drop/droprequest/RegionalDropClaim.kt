package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional drop claim is in its life cycle. */
enum class RegionalDropClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A regional drop claim. Its code is unique within a tenant. */
data class RegionalDropClaim(
    var code: String,
    var name: String,
    var windowDays: Int,
    var approved: Boolean,
    var visible: Boolean,
    var ownerId: String,
    var reviewer: String,
    var stopCount: Int,
    var status: RegionalDropClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDropClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDropClaimStatus): Boolean {
        when (status) {
            RegionalDropClaimStatus.OPEN -> {
                when (next) {
                    RegionalDropClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalDropClaimStatus.IN_REVIEW -> return next == RegionalDropClaimStatus.RESOLVED
            RegionalDropClaimStatus.RESOLVED -> {
                when (next) {
                    RegionalDropClaimStatus.REOPENED, RegionalDropClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalDropClaimStatus.REOPENED -> return next == RegionalDropClaimStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RegionalDropClaimStatus) {
        if (!canMoveTo(next)) {
            error("regional drop claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round((stopCount.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): RegionalDropClaim {
        return RegionalDropClaim(
            code = code,
            name = name,
            windowDays = windowDays,
            approved = approved,
            visible = visible,
            ownerId = ownerId,
            reviewer = reviewer,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional drop claim in status open. */
fun newRegionalDropClaim(code: String, name: String): RegionalDropClaim {
    return RegionalDropClaim(
        code = code,
        name = name,
        windowDays = 0,
        approved = false,
        visible = false,
        ownerId = "",
        reviewer = "",
        stopCount = 0,
        status = RegionalDropClaimStatus.OPEN,
    )
}
