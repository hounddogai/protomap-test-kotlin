package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The clearance variance records of customs's clearance work, and the rules, stores, and calculations around
// them.

/** Where a clearance variance is in its life cycle. */
enum class ClearanceVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A clearance variance as the clearance team keeps it. */
data class ClearanceVariance(
    var code: String,
    var label: String,
    var locale: String,
    var amount: Double,
    var expiresAt: Int,
    var depot: String,
    var pallets: Int,
    var dock: String,
    var lane: String,
    var status: ClearanceVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceVarianceStatus): Boolean {
        when (status) {
            ClearanceVarianceStatus.PROPOSED -> return next == ClearanceVarianceStatus.APPROVED || next == ClearanceVarianceStatus.WITHDRAWN
            ClearanceVarianceStatus.APPROVED -> return next == ClearanceVarianceStatus.WITHDRAWN
            ClearanceVarianceStatus.REJECTED -> return next == ClearanceVarianceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ClearanceVarianceStatus) {
        if (!canMoveTo(next)) {
            error("clearance variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedValue(): Double = round(expiresAt.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): ClearanceVariance {
        return ClearanceVariance(
            code = code,
            label = label,
            locale = locale,
            amount = amount,
            expiresAt = expiresAt,
            depot = depot,
            pallets = pallets,
            dock = dock,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a clearance variance in status proposed. */
fun newClearanceVariance(code: String, label: String): ClearanceVariance {
    return ClearanceVariance(
        code = code,
        label = label,
        locale = "",
        amount = 0.0,
        expiresAt = 0,
        depot = "",
        pallets = 0,
        dock = "",
        lane = "",
        status = ClearanceVarianceStatus.PROPOSED,
    )
}
