package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling dock door tier is in its life cycle. */
enum class RollingDockDoorTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One rolling dock door tier, with what the planners need to act on it. */
data class RollingDockDoorTier(
    var code: String,
    var name: String,
    var score: Double,
    var title: String,
    var threshold: Double,
    var checksum: String,
    var phone: String,
    var status: RollingDockDoorTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDockDoorTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDockDoorTierStatus): Boolean {
        when (status) {
            RollingDockDoorTierStatus.PROPOSED -> return next == RollingDockDoorTierStatus.REJECTED
            RollingDockDoorTierStatus.APPROVED -> {
                when (next) {
                    RollingDockDoorTierStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingDockDoorTierStatus.REJECTED -> {
                when (next) {
                    RollingDockDoorTierStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDockDoorTierStatus) {
        if (!canMoveTo(next)) {
            error("rolling dock door tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netExposure(): Double = round(score * threshold * 100) / 100

    fun snapshot(): RollingDockDoorTier {
        return RollingDockDoorTier(
            code = code,
            name = name,
            score = score,
            title = title,
            threshold = threshold,
            checksum = checksum,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling dock door tier in status proposed. */
fun newRollingDockDoorTier(code: String, name: String): RollingDockDoorTier {
    return RollingDockDoorTier(
        code = code,
        name = name,
        score = 0.0,
        title = "",
        threshold = 0.0,
        checksum = "",
        phone = "",
        status = RollingDockDoorTierStatus.PROPOSED,
    )
}
