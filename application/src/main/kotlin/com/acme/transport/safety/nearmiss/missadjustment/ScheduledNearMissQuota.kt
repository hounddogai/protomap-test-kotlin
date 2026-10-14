package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled near miss quota is in its life cycle. */
enum class ScheduledNearMissQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled near miss quota as the near miss team keeps it. */
data class ScheduledNearMissQuota(
    var code: String,
    var name: String,
    var routeCode: String,
    var budget: Double,
    var region: String,
    var batchSize: Int,
    var checksum: String,
    var fuelLiters: Double,
    var rate: Double,
    var reviewer: String,
    var status: ScheduledNearMissQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledNearMissQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledNearMissQuotaStatus): Boolean {
        when (status) {
            ScheduledNearMissQuotaStatus.DRAFT -> return next == ScheduledNearMissQuotaStatus.ACTIVE
            ScheduledNearMissQuotaStatus.PENDING -> {
                when (next) {
                    ScheduledNearMissQuotaStatus.ACTIVE -> return true
                }
                return false
            }
            ScheduledNearMissQuotaStatus.ACTIVE -> return next == ScheduledNearMissQuotaStatus.SUSPENDED
            ScheduledNearMissQuotaStatus.SUSPENDED -> return next == ScheduledNearMissQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledNearMissQuotaStatus) {
        if (!canMoveTo(next)) {
            error("scheduled near miss quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled near miss quota"} $code"

    fun projectedTotal(): Double = round((budget - batchSize.toDouble()) * 100) / 100

    fun snapshot(): ScheduledNearMissQuota {
        return ScheduledNearMissQuota(
            code = code,
            name = name,
            routeCode = routeCode,
            budget = budget,
            region = region,
            batchSize = batchSize,
            checksum = checksum,
            fuelLiters = fuelLiters,
            rate = rate,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled near miss quota in status draft. */
fun newScheduledNearMissQuota(code: String, name: String): ScheduledNearMissQuota {
    return ScheduledNearMissQuota(
        code = code,
        name = name,
        routeCode = "",
        budget = 0.0,
        region = "",
        batchSize = 0,
        checksum = "",
        fuelLiters = 0.0,
        rate = 0.0,
        reviewer = "",
        status = ScheduledNearMissQuotaStatus.DRAFT,
    )
}
