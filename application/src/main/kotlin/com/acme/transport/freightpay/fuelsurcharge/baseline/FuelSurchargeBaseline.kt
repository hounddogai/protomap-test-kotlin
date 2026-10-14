package com.acme.transport.freightpay.fuelsurcharge.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel surcharge baseline records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a fuel surcharge baseline is in its life cycle. */
enum class FuelSurchargeBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fuel surcharge baseline as the fuel surcharge team keeps it. */
data class FuelSurchargeBaseline(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var margin: Double,
    var label: String,
    var loadKg: Double,
    var shiftCode: String,
    var durationMinutes: Int,
    var stopCount: Int,
    var status: FuelSurchargeBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeBaselineStatus): Boolean {
        when (status) {
            FuelSurchargeBaselineStatus.OPEN -> {
                when (next) {
                    FuelSurchargeBaselineStatus.REOPENED -> return true
                }
                return false
            }
            FuelSurchargeBaselineStatus.IN_REVIEW -> return next == FuelSurchargeBaselineStatus.RESOLVED || next == FuelSurchargeBaselineStatus.ARCHIVED
            FuelSurchargeBaselineStatus.RESOLVED -> return next == FuelSurchargeBaselineStatus.REOPENED || next == FuelSurchargeBaselineStatus.ARCHIVED
            FuelSurchargeBaselineStatus.REOPENED -> return next == FuelSurchargeBaselineStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeBaselineStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($title)"

    fun weightedExposure(): Double = round((loadKg - margin) * 100) / 100

    fun snapshot(): FuelSurchargeBaseline {
        return FuelSurchargeBaseline(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            margin = margin,
            label = label,
            loadKg = loadKg,
            shiftCode = shiftCode,
            durationMinutes = durationMinutes,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel surcharge baseline in status open. */
fun newFuelSurchargeBaseline(code: String, title: String): FuelSurchargeBaseline {
    return FuelSurchargeBaseline(
        code = code,
        title = title,
        etaMinutes = 0,
        margin = 0.0,
        label = "",
        loadKg = 0.0,
        shiftCode = "",
        durationMinutes = 0,
        stopCount = 0,
        status = FuelSurchargeBaselineStatus.OPEN,
    )
}
