package com.acme.transport.network.zone.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled zone rollup is in its life cycle. */
enum class ScheduledZoneRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled zone rollup as the zone team keeps it. */
data class ScheduledZoneRollup(
    var code: String,
    var name: String,
    var limit: Int,
    var quantity: Int,
    var createdAt: Int,
    var status: ScheduledZoneRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledZoneRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledZoneRollupStatus): Boolean {
        when (status) {
            ScheduledZoneRollupStatus.CREATED -> return next == ScheduledZoneRollupStatus.VALIDATED || next == ScheduledZoneRollupStatus.RETIRED
            ScheduledZoneRollupStatus.VALIDATED -> {
                when (next) {
                    ScheduledZoneRollupStatus.PUBLISHED, ScheduledZoneRollupStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledZoneRollupStatus.PUBLISHED -> return next == ScheduledZoneRollupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledZoneRollupStatus) {
        if (!canMoveTo(next)) {
            error("scheduled zone rollup $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun weightedTotal(): Double = round((limit.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledZoneRollup {
        return ScheduledZoneRollup(
            code = code,
            name = name,
            limit = limit,
            quantity = quantity,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled zone rollup in status created. */
fun newScheduledZoneRollup(code: String, name: String): ScheduledZoneRollup {
    return ScheduledZoneRollup(
        code = code,
        name = name,
        limit = 0,
        quantity = 0,
        createdAt = 0,
        status = ScheduledZoneRollupStatus.CREATED,
    )
}
