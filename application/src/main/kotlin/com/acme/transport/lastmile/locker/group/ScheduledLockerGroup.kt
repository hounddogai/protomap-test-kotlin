package com.acme.transport.lastmile.locker.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled locker group records of lastmile's locker work, and the rules, stores, and calculations
// around them.

/** Where a scheduled locker group is in its life cycle. */
enum class ScheduledLockerGroupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled locker group. Its code is unique within a tenant. */
data class ScheduledLockerGroup(
    var code: String,
    var name: String,
    var driverId: String,
    var loadKg: Double,
    var updatedAt: Int,
    var region: String,
    var dock: String,
    var status: ScheduledLockerGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLockerGroupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLockerGroupStatus): Boolean {
        when (status) {
            ScheduledLockerGroupStatus.CREATED -> {
                when (next) {
                    ScheduledLockerGroupStatus.VALIDATED -> return true
                }
                return false
            }
            ScheduledLockerGroupStatus.VALIDATED -> {
                when (next) {
                    ScheduledLockerGroupStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledLockerGroupStatus.PUBLISHED -> {
                when (next) {
                    ScheduledLockerGroupStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledLockerGroupStatus) {
        if (!canMoveTo(next)) {
            error("scheduled locker group $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round((loadKg + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledLockerGroup {
        return ScheduledLockerGroup(
            code = code,
            name = name,
            driverId = driverId,
            loadKg = loadKg,
            updatedAt = updatedAt,
            region = region,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled locker group in status created. */
fun newScheduledLockerGroup(code: String, name: String): ScheduledLockerGroup {
    return ScheduledLockerGroup(
        code = code,
        name = name,
        driverId = "",
        loadKg = 0.0,
        updatedAt = 0,
        region = "",
        dock = "",
        status = ScheduledLockerGroupStatus.CREATED,
    )
}
