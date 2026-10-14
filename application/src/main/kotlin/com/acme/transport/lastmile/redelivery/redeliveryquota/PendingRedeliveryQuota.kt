package com.acme.transport.lastmile.redelivery.redeliveryquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending redelivery quota records of lastmile's redelivery work, and the rules, stores, and calculations
// around them.

/** Where a pending redelivery quota is in its life cycle. */
enum class PendingRedeliveryQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending redelivery quota as the redelivery team keeps it. */
data class PendingRedeliveryQuota(
    var code: String,
    var title: String,
    var depot: String,
    var cost: Double,
    var driverId: String,
    var ratio: Double,
    var version: Int,
    var description: String,
    var dock: String,
    var status: PendingRedeliveryQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRedeliveryQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRedeliveryQuotaStatus): Boolean {
        when (status) {
            PendingRedeliveryQuotaStatus.DRAFT -> return next == PendingRedeliveryQuotaStatus.CLOSED
            PendingRedeliveryQuotaStatus.PENDING -> return next == PendingRedeliveryQuotaStatus.SUSPENDED
            PendingRedeliveryQuotaStatus.ACTIVE -> {
                when (next) {
                    PendingRedeliveryQuotaStatus.SUSPENDED -> return true
                }
                return false
            }
            PendingRedeliveryQuotaStatus.SUSPENDED -> return next == PendingRedeliveryQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PendingRedeliveryQuotaStatus) {
        if (!canMoveTo(next)) {
            error("pending redelivery quota $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"pending redelivery quota"} $code"

    fun netVolume(): Double = round((version.toDouble() + cost) * 100) / 100

    fun snapshot(): PendingRedeliveryQuota {
        return PendingRedeliveryQuota(
            code = code,
            title = title,
            depot = depot,
            cost = cost,
            driverId = driverId,
            ratio = ratio,
            version = version,
            description = description,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a pending redelivery quota in status draft. */
fun newPendingRedeliveryQuota(code: String, title: String): PendingRedeliveryQuota {
    return PendingRedeliveryQuota(
        code = code,
        title = title,
        depot = "",
        cost = 0.0,
        driverId = "",
        ratio = 0.0,
        version = 0,
        description = "",
        dock = "",
        status = PendingRedeliveryQuotaStatus.DRAFT,
    )
}
