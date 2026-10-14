package com.acme.transport.terminal.traileryard.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy trailer yard cycle records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a legacy trailer yard cycle is in its life cycle. */
enum class LegacyTrailerYardCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One legacy trailer yard cycle, with what the planners need to act on it. */
data class LegacyTrailerYardCycle(
    var code: String,
    var name: String,
    var ownerId: String,
    var source: String,
    var etaMinutes: Int,
    var stopCount: Int,
    var sequence: Int,
    var retries: Int,
    var status: LegacyTrailerYardCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTrailerYardCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTrailerYardCycleStatus): Boolean {
        when (status) {
            LegacyTrailerYardCycleStatus.PLANNED -> {
                when (next) {
                    LegacyTrailerYardCycleStatus.IN_PROGRESS, LegacyTrailerYardCycleStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyTrailerYardCycleStatus.SCHEDULED -> return next == LegacyTrailerYardCycleStatus.IN_PROGRESS || next == LegacyTrailerYardCycleStatus.COMPLETED
            LegacyTrailerYardCycleStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyTrailerYardCycleStatus.COMPLETED, LegacyTrailerYardCycleStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyTrailerYardCycleStatus.COMPLETED -> return next == LegacyTrailerYardCycleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyTrailerYardCycleStatus) {
        if (!canMoveTo(next)) {
            error("legacy trailer yard cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy trailer yard cycle"} $code"

    fun netVolume(): Double = round(retries.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): LegacyTrailerYardCycle {
        return LegacyTrailerYardCycle(
            code = code,
            name = name,
            ownerId = ownerId,
            source = source,
            etaMinutes = etaMinutes,
            stopCount = stopCount,
            sequence = sequence,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a legacy trailer yard cycle in status planned. */
fun newLegacyTrailerYardCycle(code: String, name: String): LegacyTrailerYardCycle {
    return LegacyTrailerYardCycle(
        code = code,
        name = name,
        ownerId = "",
        source = "",
        etaMinutes = 0,
        stopCount = 0,
        sequence = 0,
        retries = 0,
        status = LegacyTrailerYardCycleStatus.PLANNED,
    )
}
