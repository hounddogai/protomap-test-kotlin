package com.acme.transport.lastmile.locker.rule

import com.acme.transport.kit.text.normalize

/** Where a fallback locker baseline is in its life cycle. */
enum class FallbackLockerBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a fallback locker baseline and its current status. */
data class FallbackLockerBaseline(
    var code: String,
    var name: String,
    var vehicleId: String,
    var title: String,
    var tags: MutableList<String>,
    var amount: Double,
    var status: FallbackLockerBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackLockerBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackLockerBaselineStatus): Boolean {
        when (status) {
            FallbackLockerBaselineStatus.IDLE -> {
                when (next) {
                    FallbackLockerBaselineStatus.WARMING, FallbackLockerBaselineStatus.DRAINING -> return true
                }
                return false
            }
            FallbackLockerBaselineStatus.WARMING -> {
                when (next) {
                    FallbackLockerBaselineStatus.READY -> return true
                }
                return false
            }
            FallbackLockerBaselineStatus.READY -> {
                when (next) {
                    FallbackLockerBaselineStatus.STOPPED -> return true
                }
                return false
            }
            FallbackLockerBaselineStatus.DRAINING -> {
                when (next) {
                    FallbackLockerBaselineStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackLockerBaselineStatus) {
        if (!canMoveTo(next)) {
            error("fallback locker baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback locker baseline"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = amount

    fun snapshot(): FallbackLockerBaseline {
        return FallbackLockerBaseline(
            code = code,
            name = name,
            vehicleId = vehicleId,
            title = title,
            tags = tags,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback locker baseline in status idle. */
fun newFallbackLockerBaseline(code: String, name: String): FallbackLockerBaseline {
    return FallbackLockerBaseline(
        code = code,
        name = name,
        vehicleId = "",
        title = "",
        tags = mutableListOf<String>(),
        amount = 0.0,
        status = FallbackLockerBaselineStatus.IDLE,
    )
}
