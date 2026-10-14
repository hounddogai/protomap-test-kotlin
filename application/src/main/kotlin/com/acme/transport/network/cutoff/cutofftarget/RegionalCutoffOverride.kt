package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional cutoff override is in its life cycle. */
enum class RegionalCutoffOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A regional cutoff override as the cutoff team keeps it. */
data class RegionalCutoffOverride(
    var code: String,
    var label: String,
    var amount: Double,
    var stopCount: Int,
    var odometer: Int,
    var lane: String,
    var currency: String,
    var trailerId: String,
    var checksum: String,
    var budget: Double,
    var status: RegionalCutoffOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCutoffOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCutoffOverrideStatus): Boolean {
        when (status) {
            RegionalCutoffOverrideStatus.NEW -> return next == RegionalCutoffOverrideStatus.RUNNING
            RegionalCutoffOverrideStatus.QUEUED -> {
                when (next) {
                    RegionalCutoffOverrideStatus.RUNNING -> return true
                }
                return false
            }
            RegionalCutoffOverrideStatus.RUNNING -> return next == RegionalCutoffOverrideStatus.FAILED
            RegionalCutoffOverrideStatus.DONE -> return next == RegionalCutoffOverrideStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalCutoffOverrideStatus) {
        if (!canMoveTo(next)) {
            error("regional cutoff override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round(stopCount.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): RegionalCutoffOverride {
        return RegionalCutoffOverride(
            code = code,
            label = label,
            amount = amount,
            stopCount = stopCount,
            odometer = odometer,
            lane = lane,
            currency = currency,
            trailerId = trailerId,
            checksum = checksum,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a regional cutoff override in status new. */
fun newRegionalCutoffOverride(code: String, label: String): RegionalCutoffOverride {
    return RegionalCutoffOverride(
        code = code,
        label = label,
        amount = 0.0,
        stopCount = 0,
        odometer = 0,
        lane = "",
        currency = "",
        trailerId = "",
        checksum = "",
        budget = 0.0,
        status = RegionalCutoffOverrideStatus.NEW,
    )
}
