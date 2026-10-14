package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient cross dock claim is in its life cycle. */
enum class LenientCrossDockClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient cross dock claim as the cross dock team keeps it. */
data class LenientCrossDockClaim(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var cost: Double,
    var routeCode: String,
    var timezone: String,
    var active: Boolean,
    var batchSize: Int,
    var trailerId: String,
    var graceDays: Int,
    var status: LenientCrossDockClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCrossDockClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCrossDockClaimStatus): Boolean {
        when (status) {
            LenientCrossDockClaimStatus.DRAFT -> return next == LenientCrossDockClaimStatus.ACTIVE
            LenientCrossDockClaimStatus.PENDING -> return next == LenientCrossDockClaimStatus.SUSPENDED
            LenientCrossDockClaimStatus.ACTIVE -> {
                when (next) {
                    LenientCrossDockClaimStatus.SUSPENDED, LenientCrossDockClaimStatus.CLOSED -> return true
                }
                return false
            }
            LenientCrossDockClaimStatus.SUSPENDED -> {
                when (next) {
                    LenientCrossDockClaimStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientCrossDockClaimStatus) {
        if (!canMoveTo(next)) {
            error("lenient cross dock claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedTotal(): Double = round(batchSize.toDouble() * cost * 100) / 100

    fun snapshot(): LenientCrossDockClaim {
        return LenientCrossDockClaim(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            cost = cost,
            routeCode = routeCode,
            timezone = timezone,
            active = active,
            batchSize = batchSize,
            trailerId = trailerId,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient cross dock claim in status draft. */
fun newLenientCrossDockClaim(code: String, name: String): LenientCrossDockClaim {
    return LenientCrossDockClaim(
        code = code,
        name = name,
        etaMinutes = 0,
        cost = 0.0,
        routeCode = "",
        timezone = "",
        active = false,
        batchSize = 0,
        trailerId = "",
        graceDays = 0,
        status = LenientCrossDockClaimStatus.DRAFT,
    )
}
