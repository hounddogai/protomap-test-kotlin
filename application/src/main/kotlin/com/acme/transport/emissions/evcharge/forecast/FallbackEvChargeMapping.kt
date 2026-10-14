package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback ev charge mapping is in its life cycle. */
enum class FallbackEvChargeMappingStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fallback ev charge mapping as the ev charge team keeps it. */
data class FallbackEvChargeMapping(
    var code: String,
    var title: String,
    var endsAt: Int,
    var cost: Double,
    var rate: Double,
    var region: String,
    var currency: String,
    var segment: String,
    var retries: Int,
    var status: FallbackEvChargeMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackEvChargeMappingStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackEvChargeMappingStatus): Boolean {
        when (status) {
            FallbackEvChargeMappingStatus.DRAFT -> return next == FallbackEvChargeMappingStatus.ACTIVE || next == FallbackEvChargeMappingStatus.SUSPENDED
            FallbackEvChargeMappingStatus.PENDING -> return next == FallbackEvChargeMappingStatus.SUSPENDED || next == FallbackEvChargeMappingStatus.CLOSED
            FallbackEvChargeMappingStatus.ACTIVE -> return next == FallbackEvChargeMappingStatus.SUSPENDED || next == FallbackEvChargeMappingStatus.CLOSED
            FallbackEvChargeMappingStatus.SUSPENDED -> return next == FallbackEvChargeMappingStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FallbackEvChargeMappingStatus) {
        if (!canMoveTo(next)) {
            error("fallback ev charge mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback ev charge mapping"} $code"

    fun adjustedLoad(): Double = round((retries.toDouble() + rate) * 100) / 100

    fun snapshot(): FallbackEvChargeMapping {
        return FallbackEvChargeMapping(
            code = code,
            title = title,
            endsAt = endsAt,
            cost = cost,
            rate = rate,
            region = region,
            currency = currency,
            segment = segment,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback ev charge mapping in status draft. */
fun newFallbackEvChargeMapping(code: String, title: String): FallbackEvChargeMapping {
    return FallbackEvChargeMapping(
        code = code,
        title = title,
        endsAt = 0,
        cost = 0.0,
        rate = 0.0,
        region = "",
        currency = "",
        segment = "",
        retries = 0,
        status = FallbackEvChargeMappingStatus.DRAFT,
    )
}
