package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional locker profile is in its life cycle. */
enum class RegionalLockerProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One regional locker profile, with what the planners need to act on it. */
data class RegionalLockerProfile(
    var code: String,
    var name: String,
    var description: String,
    var cost: Double,
    var reviewer: String,
    var title: String,
    var fuelLiters: Double,
    var timezone: String,
    var priority: Int,
    var capacity: Int,
    var tags: MutableList<String>,
    var status: RegionalLockerProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalLockerProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalLockerProfileStatus): Boolean {
        when (status) {
            RegionalLockerProfileStatus.PLANNED -> {
                when (next) {
                    RegionalLockerProfileStatus.COMPLETED, RegionalLockerProfileStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalLockerProfileStatus.SCHEDULED -> {
                when (next) {
                    RegionalLockerProfileStatus.IN_PROGRESS, RegionalLockerProfileStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalLockerProfileStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalLockerProfileStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalLockerProfileStatus.COMPLETED -> {
                when (next) {
                    RegionalLockerProfileStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalLockerProfileStatus) {
        if (!canMoveTo(next)) {
            error("regional locker profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional locker profile"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round((priority.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): RegionalLockerProfile {
        return RegionalLockerProfile(
            code = code,
            name = name,
            description = description,
            cost = cost,
            reviewer = reviewer,
            title = title,
            fuelLiters = fuelLiters,
            timezone = timezone,
            priority = priority,
            capacity = capacity,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional locker profile in status planned. */
fun newRegionalLockerProfile(code: String, name: String): RegionalLockerProfile {
    return RegionalLockerProfile(
        code = code,
        name = name,
        description = "",
        cost = 0.0,
        reviewer = "",
        title = "",
        fuelLiters = 0.0,
        timezone = "",
        priority = 0,
        capacity = 0,
        tags = mutableListOf<String>(),
        status = RegionalLockerProfileStatus.PLANNED,
    )
}
