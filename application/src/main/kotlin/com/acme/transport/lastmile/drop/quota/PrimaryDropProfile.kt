package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary drop profile is in its life cycle. */
enum class PrimaryDropProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary drop profile. Its code is unique within a tenant. */
data class PrimaryDropProfile(
    var code: String,
    var name: String,
    var cost: Double,
    var routeCode: String,
    var depot: String,
    var ceiling: Double,
    var status: PrimaryDropProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDropProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDropProfileStatus): Boolean {
        when (status) {
            PrimaryDropProfileStatus.REQUESTED -> return next == PrimaryDropProfileStatus.GRANTED
            PrimaryDropProfileStatus.GRANTED -> return next == PrimaryDropProfileStatus.EXPIRED
            PrimaryDropProfileStatus.REVOKED -> return next == PrimaryDropProfileStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryDropProfileStatus) {
        if (!canMoveTo(next)) {
            error("primary drop profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary drop profile"} $code"

    fun adjustedValue(): Double = round(cost * ceiling * 100) / 100

    fun snapshot(): PrimaryDropProfile {
        return PrimaryDropProfile(
            code = code,
            name = name,
            cost = cost,
            routeCode = routeCode,
            depot = depot,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a primary drop profile in status requested. */
fun newPrimaryDropProfile(code: String, name: String): PrimaryDropProfile {
    return PrimaryDropProfile(
        code = code,
        name = name,
        cost = 0.0,
        routeCode = "",
        depot = "",
        ceiling = 0.0,
        status = PrimaryDropProfileStatus.REQUESTED,
    )
}
