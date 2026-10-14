package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive swap schedule records of crewing's swap work, and the rules, stores, and calculations around
// them.

/** Where a adaptive swap schedule is in its life cycle. */
enum class AdaptiveSwapScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One adaptive swap schedule, with what the planners need to act on it. */
data class AdaptiveSwapSchedule(
    var code: String,
    var label: String,
    var sequence: Int,
    var capacity: Int,
    var approved: Boolean,
    var cost: Double,
    var contactEmail: String,
    var fuelLiters: Double,
    var status: AdaptiveSwapScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveSwapScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveSwapScheduleStatus): Boolean {
        when (status) {
            AdaptiveSwapScheduleStatus.NEW -> return next == AdaptiveSwapScheduleStatus.QUEUED || next == AdaptiveSwapScheduleStatus.RUNNING
            AdaptiveSwapScheduleStatus.QUEUED -> return next == AdaptiveSwapScheduleStatus.DONE || next == AdaptiveSwapScheduleStatus.FAILED
            AdaptiveSwapScheduleStatus.RUNNING -> return next == AdaptiveSwapScheduleStatus.DONE || next == AdaptiveSwapScheduleStatus.FAILED
            AdaptiveSwapScheduleStatus.DONE -> return next == AdaptiveSwapScheduleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveSwapScheduleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive swap schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netExposure(): Double = round((sequence.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): AdaptiveSwapSchedule {
        return AdaptiveSwapSchedule(
            code = code,
            label = label,
            sequence = sequence,
            capacity = capacity,
            approved = approved,
            cost = cost,
            contactEmail = contactEmail,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive swap schedule in status new. */
fun newAdaptiveSwapSchedule(code: String, label: String): AdaptiveSwapSchedule {
    return AdaptiveSwapSchedule(
        code = code,
        label = label,
        sequence = 0,
        capacity = 0,
        approved = false,
        cost = 0.0,
        contactEmail = "",
        fuelLiters = 0.0,
        status = AdaptiveSwapScheduleStatus.NEW,
    )
}
