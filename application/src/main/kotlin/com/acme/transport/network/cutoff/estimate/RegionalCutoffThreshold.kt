package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional cutoff threshold is in its life cycle. */
enum class RegionalCutoffThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A regional cutoff threshold as the cutoff team keeps it. */
data class RegionalCutoffThreshold(
    var code: String,
    var label: String,
    var summary: String,
    var quantity: Int,
    var title: String,
    var windowDays: Int,
    var etaMinutes: Int,
    var score: Double,
    var approved: Boolean,
    var maximum: Double,
    var status: RegionalCutoffThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCutoffThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCutoffThresholdStatus): Boolean {
        when (status) {
            RegionalCutoffThresholdStatus.REQUESTED -> return next == RegionalCutoffThresholdStatus.EXPIRED
            RegionalCutoffThresholdStatus.GRANTED -> return next == RegionalCutoffThresholdStatus.REVOKED || next == RegionalCutoffThresholdStatus.EXPIRED
            RegionalCutoffThresholdStatus.REVOKED -> {
                when (next) {
                    RegionalCutoffThresholdStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCutoffThresholdStatus) {
        if (!canMoveTo(next)) {
            error("regional cutoff threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round((windowDays.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): RegionalCutoffThreshold {
        return RegionalCutoffThreshold(
            code = code,
            label = label,
            summary = summary,
            quantity = quantity,
            title = title,
            windowDays = windowDays,
            etaMinutes = etaMinutes,
            score = score,
            approved = approved,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional cutoff threshold in status requested. */
fun newRegionalCutoffThreshold(code: String, label: String): RegionalCutoffThreshold {
    return RegionalCutoffThreshold(
        code = code,
        label = label,
        summary = "",
        quantity = 0,
        title = "",
        windowDays = 0,
        etaMinutes = 0,
        score = 0.0,
        approved = false,
        maximum = 0.0,
        status = RegionalCutoffThresholdStatus.REQUESTED,
    )
}
