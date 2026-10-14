package com.acme.transport.network.hub.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hub record records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub record is in its life cycle. */
enum class HubRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A hub record. Its code is unique within a tenant. */
data class HubRecord(
    var code: String,
    var label: String,
    var floorValue: Double,
    var cost: Double,
    var minimum: Double,
    var channel: String,
    var fuelLiters: Double,
    var timezone: String,
    var locale: String,
    var contactEmail: String,
    var status: HubRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubRecordStatus): Boolean {
        when (status) {
            HubRecordStatus.CREATED -> {
                when (next) {
                    HubRecordStatus.VALIDATED -> return true
                }
                return false
            }
            HubRecordStatus.VALIDATED -> return next == HubRecordStatus.PUBLISHED || next == HubRecordStatus.RETIRED
            HubRecordStatus.PUBLISHED -> return next == HubRecordStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: HubRecordStatus) {
        if (!canMoveTo(next)) {
            error("hub record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"hub record"} $code"

    fun adjustedValue(): Double = round((cost + fuelLiters) * 100) / 100

    fun snapshot(): HubRecord {
        return HubRecord(
            code = code,
            label = label,
            floorValue = floorValue,
            cost = cost,
            minimum = minimum,
            channel = channel,
            fuelLiters = fuelLiters,
            timezone = timezone,
            locale = locale,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a hub record in status created. */
fun newHubRecord(code: String, label: String): HubRecord {
    return HubRecord(
        code = code,
        label = label,
        floorValue = 0.0,
        cost = 0.0,
        minimum = 0.0,
        channel = "",
        fuelLiters = 0.0,
        timezone = "",
        locale = "",
        contactEmail = "",
        status = HubRecordStatus.CREATED,
    )
}
