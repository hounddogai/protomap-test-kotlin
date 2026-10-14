package com.acme.transport.network.zone.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy zone record is in its life cycle. */
enum class LegacyZoneRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy zone record as the zone team keeps it. */
data class LegacyZoneRecord(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var region: String,
    var routeCode: String,
    var tags: MutableList<String>,
    var capacity: Int,
    var channel: String,
    var fuelLiters: Double,
    var maximum: Double,
    var status: LegacyZoneRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyZoneRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyZoneRecordStatus): Boolean {
        when (status) {
            LegacyZoneRecordStatus.NEW -> {
                when (next) {
                    LegacyZoneRecordStatus.QUEUED, LegacyZoneRecordStatus.DONE -> return true
                }
                return false
            }
            LegacyZoneRecordStatus.QUEUED -> return next == LegacyZoneRecordStatus.DONE
            LegacyZoneRecordStatus.RUNNING -> {
                when (next) {
                    LegacyZoneRecordStatus.FAILED -> return true
                }
                return false
            }
            LegacyZoneRecordStatus.DONE -> return next == LegacyZoneRecordStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyZoneRecordStatus) {
        if (!canMoveTo(next)) {
            error("legacy zone record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round((fuelLiters + maximum) * 100) / 100

    fun snapshot(): LegacyZoneRecord {
        return LegacyZoneRecord(
            code = code,
            title = title,
            pinned = pinned,
            region = region,
            routeCode = routeCode,
            tags = tags,
            capacity = capacity,
            channel = channel,
            fuelLiters = fuelLiters,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a legacy zone record in status new. */
fun newLegacyZoneRecord(code: String, title: String): LegacyZoneRecord {
    return LegacyZoneRecord(
        code = code,
        title = title,
        pinned = false,
        region = "",
        routeCode = "",
        tags = mutableListOf<String>(),
        capacity = 0,
        channel = "",
        fuelLiters = 0.0,
        maximum = 0.0,
        status = LegacyZoneRecordStatus.NEW,
    )
}
