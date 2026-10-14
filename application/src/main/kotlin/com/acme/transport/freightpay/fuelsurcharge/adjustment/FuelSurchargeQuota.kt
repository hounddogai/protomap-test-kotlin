package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge quota is in its life cycle. */
enum class FuelSurchargeQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fuel surcharge quota, with what the planners need to act on it. */
data class FuelSurchargeQuota(
    var code: String,
    var name: String,
    var amount: Double,
    var endsAt: Int,
    var channel: String,
    var summary: String,
    var status: FuelSurchargeQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeQuotaStatus): Boolean {
        when (status) {
            FuelSurchargeQuotaStatus.OPEN -> {
                when (next) {
                    FuelSurchargeQuotaStatus.REOPENED -> return true
                }
                return false
            }
            FuelSurchargeQuotaStatus.IN_REVIEW -> {
                when (next) {
                    FuelSurchargeQuotaStatus.RESOLVED, FuelSurchargeQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
            FuelSurchargeQuotaStatus.RESOLVED -> return next == FuelSurchargeQuotaStatus.REOPENED || next == FuelSurchargeQuotaStatus.ARCHIVED
            FuelSurchargeQuotaStatus.REOPENED -> return next == FuelSurchargeQuotaStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeQuotaStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge quota $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedValue(): Double = round(endsAt.toDouble() * amount * 100) / 100

    fun snapshot(): FuelSurchargeQuota {
        return FuelSurchargeQuota(
            code = code,
            name = name,
            amount = amount,
            endsAt = endsAt,
            channel = channel,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fuel surcharge quota in status open. */
fun newFuelSurchargeQuota(code: String, name: String): FuelSurchargeQuota {
    return FuelSurchargeQuota(
        code = code,
        name = name,
        amount = 0.0,
        endsAt = 0,
        channel = "",
        summary = "",
        status = FuelSurchargeQuotaStatus.OPEN,
    )
}
