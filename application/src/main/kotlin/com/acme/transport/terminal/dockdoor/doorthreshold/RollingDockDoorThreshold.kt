package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling dock door threshold records of terminal's dock door work, and the rules, stores, and
// calculations around them.

/** Where a rolling dock door threshold is in its life cycle. */
enum class RollingDockDoorThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One rolling dock door threshold, with what the planners need to act on it. */
data class RollingDockDoorThreshold(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var revision: Int,
    var threshold: Double,
    var summary: String,
    var createdAt: Int,
    var approved: Boolean,
    var attempts: Int,
    var status: RollingDockDoorThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDockDoorThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDockDoorThresholdStatus): Boolean {
        when (status) {
            RollingDockDoorThresholdStatus.PROPOSED -> {
                when (next) {
                    RollingDockDoorThresholdStatus.APPROVED, RollingDockDoorThresholdStatus.REJECTED -> return true
                }
                return false
            }
            RollingDockDoorThresholdStatus.APPROVED -> {
                when (next) {
                    RollingDockDoorThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingDockDoorThresholdStatus.REJECTED -> {
                when (next) {
                    RollingDockDoorThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDockDoorThresholdStatus) {
        if (!canMoveTo(next)) {
            error("rolling dock door threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round((revision.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): RollingDockDoorThreshold {
        return RollingDockDoorThreshold(
            code = code,
            name = name,
            pinned = pinned,
            revision = revision,
            threshold = threshold,
            summary = summary,
            createdAt = createdAt,
            approved = approved,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling dock door threshold in status proposed. */
fun newRollingDockDoorThreshold(code: String, name: String): RollingDockDoorThreshold {
    return RollingDockDoorThreshold(
        code = code,
        name = name,
        pinned = false,
        revision = 0,
        threshold = 0.0,
        summary = "",
        createdAt = 0,
        approved = false,
        attempts = 0,
        status = RollingDockDoorThresholdStatus.PROPOSED,
    )
}
