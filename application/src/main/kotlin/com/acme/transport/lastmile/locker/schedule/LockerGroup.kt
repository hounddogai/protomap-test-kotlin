package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.kit.text.normalize

/** Where a locker group is in its life cycle. */
enum class LockerGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a locker group and its current status. */
data class LockerGroup(
    var code: String,
    var title: String,
    var timezone: String,
    var carrier: String,
    var lane: String,
    var shiftCode: String,
    var pinned: Boolean,
    var weight: Double,
    var unit: String,
    var reviewer: String,
    var status: LockerGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerGroupStatus): Boolean {
        when (status) {
            LockerGroupStatus.DRAFT -> {
                when (next) {
                    LockerGroupStatus.ACTIVE, LockerGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            LockerGroupStatus.PENDING -> return next == LockerGroupStatus.CLOSED
            LockerGroupStatus.ACTIVE -> {
                when (next) {
                    LockerGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            LockerGroupStatus.SUSPENDED -> return next == LockerGroupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LockerGroupStatus) {
        if (!canMoveTo(next)) {
            error("locker group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"locker group"} $code"

    fun measuredValue(): Double = weight

    fun snapshot(): LockerGroup {
        return LockerGroup(
            code = code,
            title = title,
            timezone = timezone,
            carrier = carrier,
            lane = lane,
            shiftCode = shiftCode,
            pinned = pinned,
            weight = weight,
            unit = unit,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a locker group in status draft. */
fun newLockerGroup(code: String, title: String): LockerGroup {
    return LockerGroup(
        code = code,
        title = title,
        timezone = "",
        carrier = "",
        lane = "",
        shiftCode = "",
        pinned = false,
        weight = 0.0,
        unit = "",
        reviewer = "",
        status = LockerGroupStatus.DRAFT,
    )
}
