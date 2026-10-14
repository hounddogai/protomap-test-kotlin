package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling ev charge quota is in its life cycle. */
enum class RollingEvChargeQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling ev charge quota as the ev charge team keeps it. */
data class RollingEvChargeQuota(
    var code: String,
    var title: String,
    var trailerId: String,
    var discount: Double,
    var country: String,
    var ceiling: Double,
    var summary: String,
    var notes: String,
    var floorValue: Double,
    var threshold: Double,
    var status: RollingEvChargeQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingEvChargeQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingEvChargeQuotaStatus): Boolean {
        when (status) {
            RollingEvChargeQuotaStatus.IDLE -> return next == RollingEvChargeQuotaStatus.WARMING
            RollingEvChargeQuotaStatus.WARMING -> return next == RollingEvChargeQuotaStatus.STOPPED
            RollingEvChargeQuotaStatus.READY -> return next == RollingEvChargeQuotaStatus.DRAINING
            RollingEvChargeQuotaStatus.DRAINING -> return next == RollingEvChargeQuotaStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingEvChargeQuotaStatus) {
        if (!canMoveTo(next)) {
            error("rolling ev charge quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedTotal(): Double = round((ceiling - threshold) * 100) / 100

    fun snapshot(): RollingEvChargeQuota {
        return RollingEvChargeQuota(
            code = code,
            title = title,
            trailerId = trailerId,
            discount = discount,
            country = country,
            ceiling = ceiling,
            summary = summary,
            notes = notes,
            floorValue = floorValue,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rolling ev charge quota in status idle. */
fun newRollingEvChargeQuota(code: String, title: String): RollingEvChargeQuota {
    return RollingEvChargeQuota(
        code = code,
        title = title,
        trailerId = "",
        discount = 0.0,
        country = "",
        ceiling = 0.0,
        summary = "",
        notes = "",
        floorValue = 0.0,
        threshold = 0.0,
        status = RollingEvChargeQuotaStatus.IDLE,
    )
}
