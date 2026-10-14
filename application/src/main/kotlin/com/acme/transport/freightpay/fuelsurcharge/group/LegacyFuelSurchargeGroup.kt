package com.acme.transport.freightpay.fuelsurcharge.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy fuel surcharge group records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a legacy fuel surcharge group is in its life cycle. */
enum class LegacyFuelSurchargeGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy fuel surcharge group as the fuel surcharge team keeps it. */
data class LegacyFuelSurchargeGroup(
    var code: String,
    var label: String,
    var notes: String,
    var threshold: Double,
    var timezone: String,
    var name: String,
    var odometer: Int,
    var pinned: Boolean,
    var windowDays: Int,
    var status: LegacyFuelSurchargeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyFuelSurchargeGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyFuelSurchargeGroupStatus): Boolean {
        when (status) {
            LegacyFuelSurchargeGroupStatus.NEW -> return next == LegacyFuelSurchargeGroupStatus.QUEUED || next == LegacyFuelSurchargeGroupStatus.FAILED
            LegacyFuelSurchargeGroupStatus.QUEUED -> {
                when (next) {
                    LegacyFuelSurchargeGroupStatus.DONE -> return true
                }
                return false
            }
            LegacyFuelSurchargeGroupStatus.RUNNING -> {
                when (next) {
                    LegacyFuelSurchargeGroupStatus.DONE, LegacyFuelSurchargeGroupStatus.FAILED -> return true
                }
                return false
            }
            LegacyFuelSurchargeGroupStatus.DONE -> return next == LegacyFuelSurchargeGroupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyFuelSurchargeGroupStatus) {
        if (!canMoveTo(next)) {
            error("legacy fuel surcharge group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedLoad(): Double = round(windowDays.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): LegacyFuelSurchargeGroup {
        return LegacyFuelSurchargeGroup(
            code = code,
            label = label,
            notes = notes,
            threshold = threshold,
            timezone = timezone,
            name = name,
            odometer = odometer,
            pinned = pinned,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy fuel surcharge group in status new. */
fun newLegacyFuelSurchargeGroup(code: String, label: String): LegacyFuelSurchargeGroup {
    return LegacyFuelSurchargeGroup(
        code = code,
        label = label,
        notes = "",
        threshold = 0.0,
        timezone = "",
        name = "",
        odometer = 0,
        pinned = false,
        windowDays = 0,
        status = LegacyFuelSurchargeGroupStatus.NEW,
    )
}
