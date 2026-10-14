package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling utilization quota is in its life cycle. */
enum class RollingUtilizationQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rolling utilization quota as the utilization team keeps it. */
data class RollingUtilizationQuota(
    var code: String,
    var name: String,
    var channel: String,
    var limit: Int,
    var weight: Double,
    var fuelLiters: Double,
    var visible: Boolean,
    var status: RollingUtilizationQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingUtilizationQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingUtilizationQuotaStatus): Boolean {
        when (status) {
            RollingUtilizationQuotaStatus.DRAFT -> return next == RollingUtilizationQuotaStatus.PENDING
            RollingUtilizationQuotaStatus.PENDING -> {
                when (next) {
                    RollingUtilizationQuotaStatus.ACTIVE, RollingUtilizationQuotaStatus.SUSPENDED -> return true
                }
                return false
            }
            RollingUtilizationQuotaStatus.ACTIVE -> {
                when (next) {
                    RollingUtilizationQuotaStatus.CLOSED -> return true
                }
                return false
            }
            RollingUtilizationQuotaStatus.SUSPENDED -> return next == RollingUtilizationQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingUtilizationQuotaStatus) {
        if (!canMoveTo(next)) {
            error("rolling utilization quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling utilization quota"} $code"

    fun adjustedTotal(): Double = round((limit.toDouble() + weight) * 100) / 100

    fun snapshot(): RollingUtilizationQuota {
        return RollingUtilizationQuota(
            code = code,
            name = name,
            channel = channel,
            limit = limit,
            weight = weight,
            fuelLiters = fuelLiters,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rolling utilization quota in status draft. */
fun newRollingUtilizationQuota(code: String, name: String): RollingUtilizationQuota {
    return RollingUtilizationQuota(
        code = code,
        name = name,
        channel = "",
        limit = 0,
        weight = 0.0,
        fuelLiters = 0.0,
        visible = false,
        status = RollingUtilizationQuotaStatus.DRAFT,
    )
}
