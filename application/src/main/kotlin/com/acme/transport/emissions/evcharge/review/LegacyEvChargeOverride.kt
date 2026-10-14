package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy ev charge override is in its life cycle. */
enum class LegacyEvChargeOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One legacy ev charge override, with what the planners need to act on it. */
data class LegacyEvChargeOverride(
    var code: String,
    var title: String,
    var discount: Double,
    var budget: Double,
    var label: String,
    var stopCount: Int,
    var carrier: String,
    var unit: String,
    var rate: Double,
    var status: LegacyEvChargeOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyEvChargeOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyEvChargeOverrideStatus): Boolean {
        when (status) {
            LegacyEvChargeOverrideStatus.OPEN -> return next == LegacyEvChargeOverrideStatus.ARCHIVED
            LegacyEvChargeOverrideStatus.IN_REVIEW -> {
                when (next) {
                    LegacyEvChargeOverrideStatus.RESOLVED -> return true
                }
                return false
            }
            LegacyEvChargeOverrideStatus.RESOLVED -> return next == LegacyEvChargeOverrideStatus.REOPENED || next == LegacyEvChargeOverrideStatus.ARCHIVED
            LegacyEvChargeOverrideStatus.REOPENED -> return next == LegacyEvChargeOverrideStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LegacyEvChargeOverrideStatus) {
        if (!canMoveTo(next)) {
            error("legacy ev charge override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($title)"

    fun effectiveTotal(): Double = round((rate - budget) * 100) / 100

    fun snapshot(): LegacyEvChargeOverride {
        return LegacyEvChargeOverride(
            code = code,
            title = title,
            discount = discount,
            budget = budget,
            label = label,
            stopCount = stopCount,
            carrier = carrier,
            unit = unit,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a legacy ev charge override in status open. */
fun newLegacyEvChargeOverride(code: String, title: String): LegacyEvChargeOverride {
    return LegacyEvChargeOverride(
        code = code,
        title = title,
        discount = 0.0,
        budget = 0.0,
        label = "",
        stopCount = 0,
        carrier = "",
        unit = "",
        rate = 0.0,
        status = LegacyEvChargeOverrideStatus.OPEN,
    )
}
