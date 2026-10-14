package com.acme.transport.crewing.roster.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster adjustment is in its life cycle. */
enum class RosterAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One roster adjustment, with what the planners need to act on it. */
data class RosterAdjustment(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var revision: Int,
    var ceiling: Double,
    var batchSize: Int,
    var sequence: Int,
    var pallets: Int,
    var status: RosterAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterAdjustmentStatus): Boolean {
        when (status) {
            RosterAdjustmentStatus.NEW -> return next == RosterAdjustmentStatus.QUEUED
            RosterAdjustmentStatus.QUEUED -> {
                when (next) {
                    RosterAdjustmentStatus.RUNNING, RosterAdjustmentStatus.DONE -> return true
                }
                return false
            }
            RosterAdjustmentStatus.RUNNING -> {
                when (next) {
                    RosterAdjustmentStatus.DONE, RosterAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            RosterAdjustmentStatus.DONE -> return next == RosterAdjustmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RosterAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("roster adjustment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun netValue(): Double = round((ceiling + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): RosterAdjustment {
        return RosterAdjustment(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            revision = revision,
            ceiling = ceiling,
            batchSize = batchSize,
            sequence = sequence,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a roster adjustment in status new. */
fun newRosterAdjustment(code: String, name: String): RosterAdjustment {
    return RosterAdjustment(
        code = code,
        name = name,
        etaMinutes = 0,
        revision = 0,
        ceiling = 0.0,
        batchSize = 0,
        sequence = 0,
        pallets = 0,
        status = RosterAdjustmentStatus.NEW,
    )
}
