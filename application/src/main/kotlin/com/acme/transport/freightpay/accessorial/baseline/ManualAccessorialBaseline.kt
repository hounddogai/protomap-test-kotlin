package com.acme.transport.freightpay.accessorial.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual accessorial baseline records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a manual accessorial baseline is in its life cycle. */
enum class ManualAccessorialBaselineStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual accessorial baseline as the accessorial team keeps it. */
data class ManualAccessorialBaseline(
    var code: String,
    var label: String,
    var discount: Double,
    var capacity: Int,
    var score: Double,
    var windowDays: Int,
    var active: Boolean,
    var budget: Double,
    var lane: String,
    var status: ManualAccessorialBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAccessorialBaselineStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAccessorialBaselineStatus): Boolean {
        when (status) {
            ManualAccessorialBaselineStatus.CREATED -> {
                when (next) {
                    ManualAccessorialBaselineStatus.VALIDATED, ManualAccessorialBaselineStatus.PUBLISHED -> return true
                }
                return false
            }
            ManualAccessorialBaselineStatus.VALIDATED -> {
                when (next) {
                    ManualAccessorialBaselineStatus.PUBLISHED -> return true
                }
                return false
            }
            ManualAccessorialBaselineStatus.PUBLISHED -> return next == ManualAccessorialBaselineStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualAccessorialBaselineStatus) {
        if (!canMoveTo(next)) {
            error("manual accessorial baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedTotal(): Double = round(discount * budget * 100) / 100

    fun snapshot(): ManualAccessorialBaseline {
        return ManualAccessorialBaseline(
            code = code,
            label = label,
            discount = discount,
            capacity = capacity,
            score = score,
            windowDays = windowDays,
            active = active,
            budget = budget,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual accessorial baseline in status created. */
fun newManualAccessorialBaseline(code: String, label: String): ManualAccessorialBaseline {
    return ManualAccessorialBaseline(
        code = code,
        label = label,
        discount = 0.0,
        capacity = 0,
        score = 0.0,
        windowDays = 0,
        active = false,
        budget = 0.0,
        lane = "",
        status = ManualAccessorialBaselineStatus.CREATED,
    )
}
