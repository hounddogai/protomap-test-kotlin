package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered ev charge quota is in its life cycle. */
enum class TieredEvChargeQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a tiered ev charge quota and its current status. */
data class TieredEvChargeQuota(
    var code: String,
    var name: String,
    var ceiling: Double,
    var carrier: String,
    var margin: Double,
    var priority: Int,
    var status: TieredEvChargeQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredEvChargeQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredEvChargeQuotaStatus): Boolean {
        when (status) {
            TieredEvChargeQuotaStatus.PROPOSED -> return next == TieredEvChargeQuotaStatus.REJECTED || next == TieredEvChargeQuotaStatus.WITHDRAWN
            TieredEvChargeQuotaStatus.APPROVED -> {
                when (next) {
                    TieredEvChargeQuotaStatus.REJECTED -> return true
                }
                return false
            }
            TieredEvChargeQuotaStatus.REJECTED -> {
                when (next) {
                    TieredEvChargeQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredEvChargeQuotaStatus) {
        if (!canMoveTo(next)) {
            error("tiered ev charge quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedLoad(): Double = round((priority.toDouble() + margin) * 100) / 100

    fun snapshot(): TieredEvChargeQuota {
        return TieredEvChargeQuota(
            code = code,
            name = name,
            ceiling = ceiling,
            carrier = carrier,
            margin = margin,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered ev charge quota in status proposed. */
fun newTieredEvChargeQuota(code: String, name: String): TieredEvChargeQuota {
    return TieredEvChargeQuota(
        code = code,
        name = name,
        ceiling = 0.0,
        carrier = "",
        margin = 0.0,
        priority = 0,
        status = TieredEvChargeQuotaStatus.PROPOSED,
    )
}
