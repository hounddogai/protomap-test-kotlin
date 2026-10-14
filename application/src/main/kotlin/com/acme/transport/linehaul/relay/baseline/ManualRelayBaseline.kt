package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual relay baseline records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a manual relay baseline is in its life cycle. */
enum class ManualRelayBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A manual relay baseline as the relay team keeps it. */
data class ManualRelayBaseline(
    var code: String,
    var name: String,
    var windowDays: Int,
    var carrier: String,
    var fuelLiters: Double,
    var sequence: Int,
    var lane: String,
    var status: ManualRelayBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRelayBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRelayBaselineStatus): Boolean {
        when (status) {
            ManualRelayBaselineStatus.OPEN -> return next == ManualRelayBaselineStatus.ARCHIVED
            ManualRelayBaselineStatus.IN_REVIEW -> {
                when (next) {
                    ManualRelayBaselineStatus.RESOLVED -> return true
                }
                return false
            }
            ManualRelayBaselineStatus.RESOLVED -> return next == ManualRelayBaselineStatus.REOPENED
            ManualRelayBaselineStatus.REOPENED -> {
                when (next) {
                    ManualRelayBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualRelayBaselineStatus) {
        if (!canMoveTo(next)) {
            error("manual relay baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round((windowDays.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): ManualRelayBaseline {
        return ManualRelayBaseline(
            code = code,
            name = name,
            windowDays = windowDays,
            carrier = carrier,
            fuelLiters = fuelLiters,
            sequence = sequence,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual relay baseline in status open. */
fun newManualRelayBaseline(code: String, name: String): ManualRelayBaseline {
    return ManualRelayBaseline(
        code = code,
        name = name,
        windowDays = 0,
        carrier = "",
        fuelLiters = 0.0,
        sequence = 0,
        lane = "",
        status = ManualRelayBaselineStatus.OPEN,
    )
}
