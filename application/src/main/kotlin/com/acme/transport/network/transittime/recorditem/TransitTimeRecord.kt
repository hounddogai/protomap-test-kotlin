package com.acme.transport.network.transittime.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The transit time record records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time record is in its life cycle. */
enum class TransitTimeRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a transit time record and its current status. */
data class TransitTimeRecord(
    var code: String,
    var label: String,
    var channel: String,
    var ceiling: Double,
    var contactEmail: String,
    var routeCode: String,
    var region: String,
    var floorValue: Double,
    var status: TransitTimeRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeRecordStatus): Boolean {
        when (status) {
            TransitTimeRecordStatus.REQUESTED -> {
                when (next) {
                    TransitTimeRecordStatus.GRANTED -> return true
                }
                return false
            }
            TransitTimeRecordStatus.GRANTED -> return next == TransitTimeRecordStatus.REVOKED || next == TransitTimeRecordStatus.EXPIRED
            TransitTimeRecordStatus.REVOKED -> return next == TransitTimeRecordStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TransitTimeRecordStatus) {
        if (!canMoveTo(next)) {
            error("transit time record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"transit time record"} $code"

    fun weightedVolume(): Double = round((floorValue - ceiling) * 100) / 100

    fun snapshot(): TransitTimeRecord {
        return TransitTimeRecord(
            code = code,
            label = label,
            channel = channel,
            ceiling = ceiling,
            contactEmail = contactEmail,
            routeCode = routeCode,
            region = region,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a transit time record in status requested. */
fun newTransitTimeRecord(code: String, label: String): TransitTimeRecord {
    return TransitTimeRecord(
        code = code,
        label = label,
        channel = "",
        ceiling = 0.0,
        contactEmail = "",
        routeCode = "",
        region = "",
        floorValue = 0.0,
        status = TransitTimeRecordStatus.REQUESTED,
    )
}
