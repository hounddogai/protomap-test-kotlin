package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted utilization quota is in its life cycle. */
enum class WeightedUtilizationQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a weighted utilization quota and its current status. */
data class WeightedUtilizationQuota(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var quantity: Int,
    var summary: String,
    var ownerId: String,
    var cost: Double,
    var createdAt: Int,
    var status: WeightedUtilizationQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedUtilizationQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedUtilizationQuotaStatus): Boolean {
        when (status) {
            WeightedUtilizationQuotaStatus.DRAFT -> return next == WeightedUtilizationQuotaStatus.PENDING
            WeightedUtilizationQuotaStatus.PENDING -> return next == WeightedUtilizationQuotaStatus.CLOSED
            WeightedUtilizationQuotaStatus.ACTIVE -> {
                when (next) {
                    WeightedUtilizationQuotaStatus.SUSPENDED, WeightedUtilizationQuotaStatus.CLOSED -> return true
                }
                return false
            }
            WeightedUtilizationQuotaStatus.SUSPENDED -> {
                when (next) {
                    WeightedUtilizationQuotaStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedUtilizationQuotaStatus) {
        if (!canMoveTo(next)) {
            error("weighted utilization quota $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun effectiveLoad(): Double = round(cost * createdAt.toDouble() * 100) / 100

    fun snapshot(): WeightedUtilizationQuota {
        return WeightedUtilizationQuota(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            quantity = quantity,
            summary = summary,
            ownerId = ownerId,
            cost = cost,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted utilization quota in status draft. */
fun newWeightedUtilizationQuota(code: String, title: String): WeightedUtilizationQuota {
    return WeightedUtilizationQuota(
        code = code,
        title = title,
        etaMinutes = 0,
        quantity = 0,
        summary = "",
        ownerId = "",
        cost = 0.0,
        createdAt = 0,
        status = WeightedUtilizationQuotaStatus.DRAFT,
    )
}
