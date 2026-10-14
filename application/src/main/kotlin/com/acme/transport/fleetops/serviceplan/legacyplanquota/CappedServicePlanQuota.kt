package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped service plan quota records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a capped service plan quota is in its life cycle. */
enum class CappedServicePlanQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a capped service plan quota and its current status. */
data class CappedServicePlanQuota(
    var code: String,
    var title: String,
    var source: String,
    var etaMinutes: Int,
    var endsAt: Int,
    var notes: String,
    var batchSize: Int,
    var status: CappedServicePlanQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedServicePlanQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedServicePlanQuotaStatus): Boolean {
        when (status) {
            CappedServicePlanQuotaStatus.DRAFT -> {
                when (next) {
                    CappedServicePlanQuotaStatus.CLOSED -> return true
                }
                return false
            }
            CappedServicePlanQuotaStatus.PENDING -> {
                when (next) {
                    CappedServicePlanQuotaStatus.SUSPENDED, CappedServicePlanQuotaStatus.CLOSED -> return true
                }
                return false
            }
            CappedServicePlanQuotaStatus.ACTIVE -> return next == CappedServicePlanQuotaStatus.SUSPENDED
            CappedServicePlanQuotaStatus.SUSPENDED -> {
                when (next) {
                    CappedServicePlanQuotaStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedServicePlanQuotaStatus) {
        if (!canMoveTo(next)) {
            error("capped service plan quota $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedValue(): Double = round(batchSize.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): CappedServicePlanQuota {
        return CappedServicePlanQuota(
            code = code,
            title = title,
            source = source,
            etaMinutes = etaMinutes,
            endsAt = endsAt,
            notes = notes,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capped service plan quota in status draft. */
fun newCappedServicePlanQuota(code: String, title: String): CappedServicePlanQuota {
    return CappedServicePlanQuota(
        code = code,
        title = title,
        source = "",
        etaMinutes = 0,
        endsAt = 0,
        notes = "",
        batchSize = 0,
        status = CappedServicePlanQuotaStatus.DRAFT,
    )
}
