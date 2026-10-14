package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped relay schedule is in its life cycle. */
enum class CappedRelayScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capped relay schedule as the relay team keeps it. */
data class CappedRelaySchedule(
    var code: String,
    var title: String,
    var region: String,
    var country: String,
    var version: Int,
    var cost: Double,
    var description: String,
    var carrier: String,
    var maximum: Double,
    var stopCount: Int,
    var status: CappedRelayScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRelayScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRelayScheduleStatus): Boolean {
        when (status) {
            CappedRelayScheduleStatus.CREATED -> return next == CappedRelayScheduleStatus.PUBLISHED
            CappedRelayScheduleStatus.VALIDATED -> {
                when (next) {
                    CappedRelayScheduleStatus.PUBLISHED -> return true
                }
                return false
            }
            CappedRelayScheduleStatus.PUBLISHED -> {
                when (next) {
                    CappedRelayScheduleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedRelayScheduleStatus) {
        if (!canMoveTo(next)) {
            error("capped relay schedule $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun effectiveExposure(): Double = round((maximum - version.toDouble()) * 100) / 100

    fun snapshot(): CappedRelaySchedule {
        return CappedRelaySchedule(
            code = code,
            title = title,
            region = region,
            country = country,
            version = version,
            cost = cost,
            description = description,
            carrier = carrier,
            maximum = maximum,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capped relay schedule in status created. */
fun newCappedRelaySchedule(code: String, title: String): CappedRelaySchedule {
    return CappedRelaySchedule(
        code = code,
        title = title,
        region = "",
        country = "",
        version = 0,
        cost = 0.0,
        description = "",
        carrier = "",
        maximum = 0.0,
        stopCount = 0,
        status = CappedRelayScheduleStatus.CREATED,
    )
}
