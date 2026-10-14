package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback fuel surcharge quota is in its life cycle. */
enum class FallbackFuelSurchargeQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fallback fuel surcharge quota. Its code is unique within a tenant. */
data class FallbackFuelSurchargeQuota(
    var code: String,
    var name: String,
    var channel: String,
    var trailerId: String,
    var loadKg: Double,
    var endsAt: Int,
    var pinned: Boolean,
    var currency: String,
    var status: FallbackFuelSurchargeQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackFuelSurchargeQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackFuelSurchargeQuotaStatus): Boolean {
        when (status) {
            FallbackFuelSurchargeQuotaStatus.DRAFT -> {
                when (next) {
                    FallbackFuelSurchargeQuotaStatus.PENDING, FallbackFuelSurchargeQuotaStatus.CLOSED -> return true
                }
                return false
            }
            FallbackFuelSurchargeQuotaStatus.PENDING -> {
                when (next) {
                    FallbackFuelSurchargeQuotaStatus.CLOSED -> return true
                }
                return false
            }
            FallbackFuelSurchargeQuotaStatus.ACTIVE -> return next == FallbackFuelSurchargeQuotaStatus.SUSPENDED || next == FallbackFuelSurchargeQuotaStatus.CLOSED
            FallbackFuelSurchargeQuotaStatus.SUSPENDED -> {
                when (next) {
                    FallbackFuelSurchargeQuotaStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackFuelSurchargeQuotaStatus) {
        if (!canMoveTo(next)) {
            error("fallback fuel surcharge quota $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round(endsAt.toDouble() * loadKg * 100) / 100

    fun snapshot(): FallbackFuelSurchargeQuota {
        return FallbackFuelSurchargeQuota(
            code = code,
            name = name,
            channel = channel,
            trailerId = trailerId,
            loadKg = loadKg,
            endsAt = endsAt,
            pinned = pinned,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fallback fuel surcharge quota in status draft. */
fun newFallbackFuelSurchargeQuota(code: String, name: String): FallbackFuelSurchargeQuota {
    return FallbackFuelSurchargeQuota(
        code = code,
        name = name,
        channel = "",
        trailerId = "",
        loadKg = 0.0,
        endsAt = 0,
        pinned = false,
        currency = "",
        status = FallbackFuelSurchargeQuotaStatus.DRAFT,
    )
}
