package com.acme.transport.lastmile.locker.quota

import com.acme.transport.kit.text.normalize

/** Where a locker threshold is in its life cycle. */
enum class LockerThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One locker threshold, with what the planners need to act on it. */
data class LockerThreshold(
    var code: String,
    var name: String,
    var weight: Double,
    var timezone: String,
    var shiftCode: String,
    var tags: MutableList<String>,
    var ownerId: String,
    var status: LockerThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerThresholdStatus): Boolean {
        when (status) {
            LockerThresholdStatus.NEW -> {
                when (next) {
                    LockerThresholdStatus.QUEUED, LockerThresholdStatus.DONE -> return true
                }
                return false
            }
            LockerThresholdStatus.QUEUED -> return next == LockerThresholdStatus.RUNNING
            LockerThresholdStatus.RUNNING -> {
                when (next) {
                    LockerThresholdStatus.DONE, LockerThresholdStatus.FAILED -> return true
                }
                return false
            }
            LockerThresholdStatus.DONE -> return next == LockerThresholdStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LockerThresholdStatus) {
        if (!canMoveTo(next)) {
            error("locker threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"locker threshold"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = weight

    fun snapshot(): LockerThreshold {
        return LockerThreshold(
            code = code,
            name = name,
            weight = weight,
            timezone = timezone,
            shiftCode = shiftCode,
            tags = tags,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a locker threshold in status new. */
fun newLockerThreshold(code: String, name: String): LockerThreshold {
    return LockerThreshold(
        code = code,
        name = name,
        weight = 0.0,
        timezone = "",
        shiftCode = "",
        tags = mutableListOf<String>(),
        ownerId = "",
        status = LockerThresholdStatus.NEW,
    )
}
