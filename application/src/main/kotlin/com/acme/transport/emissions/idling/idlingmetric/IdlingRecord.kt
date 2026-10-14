package com.acme.transport.emissions.idling.idlingmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling record is in its life cycle. */
enum class IdlingRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a idling record and its current status. */
data class IdlingRecord(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var routeCode: String,
    var retries: Int,
    var channel: String,
    var description: String,
    var updatedAt: Int,
    var status: IdlingRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingRecordStatus): Boolean {
        when (status) {
            IdlingRecordStatus.CREATED -> {
                when (next) {
                    IdlingRecordStatus.VALIDATED -> return true
                }
                return false
            }
            IdlingRecordStatus.VALIDATED -> {
                when (next) {
                    IdlingRecordStatus.PUBLISHED -> return true
                }
                return false
            }
            IdlingRecordStatus.PUBLISHED -> return next == IdlingRecordStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: IdlingRecordStatus) {
        if (!canMoveTo(next)) {
            error("idling record $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedLoad(): Double = round((retries.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): IdlingRecord {
        return IdlingRecord(
            code = code,
            label = label,
            distanceKm = distanceKm,
            routeCode = routeCode,
            retries = retries,
            channel = channel,
            description = description,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a idling record in status created. */
fun newIdlingRecord(code: String, label: String): IdlingRecord {
    return IdlingRecord(
        code = code,
        label = label,
        distanceKm = 0.0,
        routeCode = "",
        retries = 0,
        channel = "",
        description = "",
        updatedAt = 0,
        status = IdlingRecordStatus.CREATED,
    )
}
