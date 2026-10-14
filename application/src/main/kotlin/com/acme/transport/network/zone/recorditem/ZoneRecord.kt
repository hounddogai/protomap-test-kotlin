package com.acme.transport.network.zone.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone record records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone record is in its life cycle. */
enum class ZoneRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A zone record. Its code is unique within a tenant. */
data class ZoneRecord(
    var code: String,
    var name: String,
    var timezone: String,
    var vehicleId: String,
    var routeCode: String,
    var expiresAt: Int,
    var durationMinutes: Int,
    var title: String,
    var version: Int,
    var ratio: Double,
    var status: ZoneRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneRecordStatus): Boolean {
        when (status) {
            ZoneRecordStatus.CREATED -> return next == ZoneRecordStatus.VALIDATED
            ZoneRecordStatus.VALIDATED -> {
                when (next) {
                    ZoneRecordStatus.PUBLISHED, ZoneRecordStatus.RETIRED -> return true
                }
                return false
            }
            ZoneRecordStatus.PUBLISHED -> return next == ZoneRecordStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ZoneRecordStatus) {
        if (!canMoveTo(next)) {
            error("zone record $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $name"

    fun projectedVolume(): Double = round((version.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ZoneRecord {
        return ZoneRecord(
            code = code,
            name = name,
            timezone = timezone,
            vehicleId = vehicleId,
            routeCode = routeCode,
            expiresAt = expiresAt,
            durationMinutes = durationMinutes,
            title = title,
            version = version,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a zone record in status created. */
fun newZoneRecord(code: String, name: String): ZoneRecord {
    return ZoneRecord(
        code = code,
        name = name,
        timezone = "",
        vehicleId = "",
        routeCode = "",
        expiresAt = 0,
        durationMinutes = 0,
        title = "",
        version = 0,
        ratio = 0.0,
        status = ZoneRecordStatus.CREATED,
    )
}
