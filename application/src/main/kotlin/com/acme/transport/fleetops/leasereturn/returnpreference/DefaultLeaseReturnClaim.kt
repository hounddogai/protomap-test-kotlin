package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default lease return claim is in its life cycle. */
enum class DefaultLeaseReturnClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One default lease return claim, with what the planners need to act on it. */
data class DefaultLeaseReturnClaim(
    var code: String,
    var title: String,
    var unit: String,
    var graceDays: Int,
    var expiresAt: Int,
    var category: String,
    var ceiling: Double,
    var status: DefaultLeaseReturnClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultLeaseReturnClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultLeaseReturnClaimStatus): Boolean {
        when (status) {
            DefaultLeaseReturnClaimStatus.CREATED -> return next == DefaultLeaseReturnClaimStatus.VALIDATED || next == DefaultLeaseReturnClaimStatus.PUBLISHED
            DefaultLeaseReturnClaimStatus.VALIDATED -> {
                when (next) {
                    DefaultLeaseReturnClaimStatus.PUBLISHED -> return true
                }
                return false
            }
            DefaultLeaseReturnClaimStatus.PUBLISHED -> return next == DefaultLeaseReturnClaimStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultLeaseReturnClaimStatus) {
        if (!canMoveTo(next)) {
            error("default lease return claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedTotal(): Double = round((expiresAt.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): DefaultLeaseReturnClaim {
        return DefaultLeaseReturnClaim(
            code = code,
            title = title,
            unit = unit,
            graceDays = graceDays,
            expiresAt = expiresAt,
            category = category,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a default lease return claim in status created. */
fun newDefaultLeaseReturnClaim(code: String, title: String): DefaultLeaseReturnClaim {
    return DefaultLeaseReturnClaim(
        code = code,
        title = title,
        unit = "",
        graceDays = 0,
        expiresAt = 0,
        category = "",
        ceiling = 0.0,
        status = DefaultLeaseReturnClaimStatus.CREATED,
    )
}
