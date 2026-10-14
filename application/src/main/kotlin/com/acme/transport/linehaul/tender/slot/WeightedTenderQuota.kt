package com.acme.transport.linehaul.tender.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted tender quota is in its life cycle. */
enum class WeightedTenderQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A weighted tender quota. Its code is unique within a tenant. */
data class WeightedTenderQuota(
    var code: String,
    var title: String,
    var floorValue: Double,
    var margin: Double,
    var summary: String,
    var weight: Double,
    var version: Int,
    var country: String,
    var status: WeightedTenderQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTenderQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTenderQuotaStatus): Boolean {
        when (status) {
            WeightedTenderQuotaStatus.PROPOSED -> {
                when (next) {
                    WeightedTenderQuotaStatus.APPROVED -> return true
                }
                return false
            }
            WeightedTenderQuotaStatus.APPROVED -> {
                when (next) {
                    WeightedTenderQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedTenderQuotaStatus.REJECTED -> return next == WeightedTenderQuotaStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: WeightedTenderQuotaStatus) {
        if (!canMoveTo(next)) {
            error("weighted tender quota $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"weighted tender quota"} $code"

    fun weightedVolume(): Double = round(weight * margin * 100) / 100

    fun snapshot(): WeightedTenderQuota {
        return WeightedTenderQuota(
            code = code,
            title = title,
            floorValue = floorValue,
            margin = margin,
            summary = summary,
            weight = weight,
            version = version,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a weighted tender quota in status proposed. */
fun newWeightedTenderQuota(code: String, title: String): WeightedTenderQuota {
    return WeightedTenderQuota(
        code = code,
        title = title,
        floorValue = 0.0,
        margin = 0.0,
        summary = "",
        weight = 0.0,
        version = 0,
        country = "",
        status = WeightedTenderQuotaStatus.PROPOSED,
    )
}
