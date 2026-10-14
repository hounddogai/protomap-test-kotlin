package com.acme.transport.network.zone.pendingzonemapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict zone record is in its life cycle. */
enum class StrictZoneRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A strict zone record as the zone team keeps it. */
data class StrictZoneRecord(
    var code: String,
    var title: String,
    var stopCount: Int,
    var vehicleId: String,
    var loadKg: Double,
    var quantity: Int,
    var contactEmail: String,
    var budget: Double,
    var status: StrictZoneRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictZoneRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictZoneRecordStatus): Boolean {
        when (status) {
            StrictZoneRecordStatus.IDLE -> return next == StrictZoneRecordStatus.STOPPED
            StrictZoneRecordStatus.WARMING -> {
                when (next) {
                    StrictZoneRecordStatus.READY -> return true
                }
                return false
            }
            StrictZoneRecordStatus.READY -> {
                when (next) {
                    StrictZoneRecordStatus.DRAINING, StrictZoneRecordStatus.STOPPED -> return true
                }
                return false
            }
            StrictZoneRecordStatus.DRAINING -> {
                when (next) {
                    StrictZoneRecordStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictZoneRecordStatus) {
        if (!canMoveTo(next)) {
            error("strict zone record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict zone record"} $code"

    fun effectiveValue(): Double = round((loadKg + quantity.toDouble()) * 100) / 100

    fun snapshot(): StrictZoneRecord {
        return StrictZoneRecord(
            code = code,
            title = title,
            stopCount = stopCount,
            vehicleId = vehicleId,
            loadKg = loadKg,
            quantity = quantity,
            contactEmail = contactEmail,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict zone record in status idle. */
fun newStrictZoneRecord(code: String, title: String): StrictZoneRecord {
    return StrictZoneRecord(
        code = code,
        title = title,
        stopCount = 0,
        vehicleId = "",
        loadKg = 0.0,
        quantity = 0,
        contactEmail = "",
        budget = 0.0,
        status = StrictZoneRecordStatus.IDLE,
    )
}
