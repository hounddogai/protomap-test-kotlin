package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global fuel surcharge baseline is in its life cycle. */
enum class GlobalFuelSurchargeBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global fuel surcharge baseline as the fuel surcharge team keeps it. */
data class GlobalFuelSurchargeBaseline(
    var code: String,
    var title: String,
    var cost: Double,
    var carrier: String,
    var etaMinutes: Int,
    var capacity: Int,
    var version: Int,
    var status: GlobalFuelSurchargeBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalFuelSurchargeBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalFuelSurchargeBaselineStatus): Boolean {
        when (status) {
            GlobalFuelSurchargeBaselineStatus.DRAFT -> {
                when (next) {
                    GlobalFuelSurchargeBaselineStatus.ACTIVE, GlobalFuelSurchargeBaselineStatus.SUSPENDED -> return true
                }
                return false
            }
            GlobalFuelSurchargeBaselineStatus.PENDING -> return next == GlobalFuelSurchargeBaselineStatus.SUSPENDED
            GlobalFuelSurchargeBaselineStatus.ACTIVE -> return next == GlobalFuelSurchargeBaselineStatus.CLOSED
            GlobalFuelSurchargeBaselineStatus.SUSPENDED -> {
                when (next) {
                    GlobalFuelSurchargeBaselineStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalFuelSurchargeBaselineStatus) {
        if (!canMoveTo(next)) {
            error("global fuel surcharge baseline $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedExposure(): Double = round(capacity.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): GlobalFuelSurchargeBaseline {
        return GlobalFuelSurchargeBaseline(
            code = code,
            title = title,
            cost = cost,
            carrier = carrier,
            etaMinutes = etaMinutes,
            capacity = capacity,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global fuel surcharge baseline in status draft. */
fun newGlobalFuelSurchargeBaseline(code: String, title: String): GlobalFuelSurchargeBaseline {
    return GlobalFuelSurchargeBaseline(
        code = code,
        title = title,
        cost = 0.0,
        carrier = "",
        etaMinutes = 0,
        capacity = 0,
        version = 0,
        status = GlobalFuelSurchargeBaselineStatus.DRAFT,
    )
}
