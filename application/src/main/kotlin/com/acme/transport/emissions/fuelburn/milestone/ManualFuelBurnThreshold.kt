package com.acme.transport.emissions.fuelburn.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual fuel burn threshold is in its life cycle. */
enum class ManualFuelBurnThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a manual fuel burn threshold and its current status. */
data class ManualFuelBurnThreshold(
    var code: String,
    var title: String,
    var weight: Double,
    var dock: String,
    var revision: Int,
    var name: String,
    var margin: Double,
    var phone: String,
    var status: ManualFuelBurnThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualFuelBurnThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualFuelBurnThresholdStatus): Boolean {
        when (status) {
            ManualFuelBurnThresholdStatus.IDLE -> return next == ManualFuelBurnThresholdStatus.DRAINING || next == ManualFuelBurnThresholdStatus.STOPPED
            ManualFuelBurnThresholdStatus.WARMING -> return next == ManualFuelBurnThresholdStatus.DRAINING || next == ManualFuelBurnThresholdStatus.STOPPED
            ManualFuelBurnThresholdStatus.READY -> return next == ManualFuelBurnThresholdStatus.DRAINING
            ManualFuelBurnThresholdStatus.DRAINING -> return next == ManualFuelBurnThresholdStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualFuelBurnThresholdStatus) {
        if (!canMoveTo(next)) {
            error("manual fuel burn threshold $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"manual fuel burn threshold"} $code"

    fun effectiveExposure(): Double = round((weight - margin) * 100) / 100

    fun snapshot(): ManualFuelBurnThreshold {
        return ManualFuelBurnThreshold(
            code = code,
            title = title,
            weight = weight,
            dock = dock,
            revision = revision,
            name = name,
            margin = margin,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual fuel burn threshold in status idle. */
fun newManualFuelBurnThreshold(code: String, title: String): ManualFuelBurnThreshold {
    return ManualFuelBurnThreshold(
        code = code,
        title = title,
        weight = 0.0,
        dock = "",
        revision = 0,
        name = "",
        margin = 0.0,
        phone = "",
        status = ManualFuelBurnThresholdStatus.IDLE,
    )
}
