package com.acme.transport.freightpay.accessorial.accessorialcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional accessorial cycle records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a regional accessorial cycle is in its life cycle. */
enum class RegionalAccessorialCycleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A regional accessorial cycle as the accessorial team keeps it. */
data class RegionalAccessorialCycle(
    var code: String,
    var title: String,
    var threshold: Double,
    var ratio: Double,
    var currency: String,
    var lane: String,
    var summary: String,
    var approved: Boolean,
    var status: RegionalAccessorialCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalAccessorialCycleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalAccessorialCycleStatus): Boolean {
        when (status) {
            RegionalAccessorialCycleStatus.NEW -> return next == RegionalAccessorialCycleStatus.RUNNING || next == RegionalAccessorialCycleStatus.DONE
            RegionalAccessorialCycleStatus.QUEUED -> {
                when (next) {
                    RegionalAccessorialCycleStatus.RUNNING -> return true
                }
                return false
            }
            RegionalAccessorialCycleStatus.RUNNING -> {
                when (next) {
                    RegionalAccessorialCycleStatus.DONE, RegionalAccessorialCycleStatus.FAILED -> return true
                }
                return false
            }
            RegionalAccessorialCycleStatus.DONE -> return next == RegionalAccessorialCycleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalAccessorialCycleStatus) {
        if (!canMoveTo(next)) {
            error("regional accessorial cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveExposure(): Double = round(threshold * ratio * 100) / 100

    fun snapshot(): RegionalAccessorialCycle {
        return RegionalAccessorialCycle(
            code = code,
            title = title,
            threshold = threshold,
            ratio = ratio,
            currency = currency,
            lane = lane,
            summary = summary,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a regional accessorial cycle in status new. */
fun newRegionalAccessorialCycle(code: String, title: String): RegionalAccessorialCycle {
    return RegionalAccessorialCycle(
        code = code,
        title = title,
        threshold = 0.0,
        ratio = 0.0,
        currency = "",
        lane = "",
        summary = "",
        approved = false,
        status = RegionalAccessorialCycleStatus.NEW,
    )
}
