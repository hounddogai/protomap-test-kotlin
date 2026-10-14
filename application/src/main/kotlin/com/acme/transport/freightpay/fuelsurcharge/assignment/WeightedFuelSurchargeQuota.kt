package com.acme.transport.freightpay.fuelsurcharge.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted fuel surcharge quota is in its life cycle. */
enum class WeightedFuelSurchargeQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted fuel surcharge quota. Its code is unique within a tenant. */
data class WeightedFuelSurchargeQuota(
    var code: String,
    var name: String,
    var margin: Double,
    var windowDays: Int,
    var minimum: Double,
    var status: WeightedFuelSurchargeQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFuelSurchargeQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFuelSurchargeQuotaStatus): Boolean {
        when (status) {
            WeightedFuelSurchargeQuotaStatus.DRAFT -> return next == WeightedFuelSurchargeQuotaStatus.ACTIVE || next == WeightedFuelSurchargeQuotaStatus.SUSPENDED
            WeightedFuelSurchargeQuotaStatus.PENDING -> {
                when (next) {
                    WeightedFuelSurchargeQuotaStatus.CLOSED -> return true
                }
                return false
            }
            WeightedFuelSurchargeQuotaStatus.ACTIVE -> {
                when (next) {
                    WeightedFuelSurchargeQuotaStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedFuelSurchargeQuotaStatus.SUSPENDED -> return next == WeightedFuelSurchargeQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: WeightedFuelSurchargeQuotaStatus) {
        if (!canMoveTo(next)) {
            error("weighted fuel surcharge quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted fuel surcharge quota"} $code"

    fun weightedLoad(): Double = round(minimum * windowDays.toDouble() * 100) / 100

    fun snapshot(): WeightedFuelSurchargeQuota {
        return WeightedFuelSurchargeQuota(
            code = code,
            name = name,
            margin = margin,
            windowDays = windowDays,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted fuel surcharge quota in status draft. */
fun newWeightedFuelSurchargeQuota(code: String, name: String): WeightedFuelSurchargeQuota {
    return WeightedFuelSurchargeQuota(
        code = code,
        name = name,
        margin = 0.0,
        windowDays = 0,
        minimum = 0.0,
        status = WeightedFuelSurchargeQuotaStatus.DRAFT,
    )
}
