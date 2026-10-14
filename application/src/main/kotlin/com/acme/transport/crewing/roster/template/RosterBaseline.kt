package com.acme.transport.crewing.roster.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster baseline is in its life cycle. */
enum class RosterBaselineStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A roster baseline. Its code is unique within a tenant. */
data class RosterBaseline(
    var code: String,
    var label: String,
    var trailerId: String,
    var endsAt: Int,
    var rate: Double,
    var reviewer: String,
    var odometer: Int,
    var score: Double,
    var status: RosterBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterBaselineStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterBaselineStatus): Boolean {
        when (status) {
            RosterBaselineStatus.NEW -> return next == RosterBaselineStatus.RUNNING || next == RosterBaselineStatus.DONE
            RosterBaselineStatus.QUEUED -> {
                when (next) {
                    RosterBaselineStatus.DONE -> return true
                }
                return false
            }
            RosterBaselineStatus.RUNNING -> return next == RosterBaselineStatus.FAILED
            RosterBaselineStatus.DONE -> return next == RosterBaselineStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RosterBaselineStatus) {
        if (!canMoveTo(next)) {
            error("roster baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netTotal(): Double = round((score + rate) * 100) / 100

    fun snapshot(): RosterBaseline {
        return RosterBaseline(
            code = code,
            label = label,
            trailerId = trailerId,
            endsAt = endsAt,
            rate = rate,
            reviewer = reviewer,
            odometer = odometer,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a roster baseline in status new. */
fun newRosterBaseline(code: String, label: String): RosterBaseline {
    return RosterBaseline(
        code = code,
        label = label,
        trailerId = "",
        endsAt = 0,
        rate = 0.0,
        reviewer = "",
        odometer = 0,
        score = 0.0,
        status = RosterBaselineStatus.NEW,
    )
}
