package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled dock door record records of terminal's dock door work, and the rules, stores, and
// calculations around them.

/** Where a scheduled dock door record is in its life cycle. */
enum class ScheduledDockDoorRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scheduled dock door record as the dock door team keeps it. */
data class ScheduledDockDoorRecord(
    var code: String,
    var name: String,
    var score: Double,
    var endsAt: Int,
    var notes: String,
    var floorValue: Double,
    var odometer: Int,
    var expiresAt: Int,
    var description: String,
    var status: ScheduledDockDoorRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDockDoorRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDockDoorRecordStatus): Boolean {
        when (status) {
            ScheduledDockDoorRecordStatus.REQUESTED -> {
                when (next) {
                    ScheduledDockDoorRecordStatus.REVOKED, ScheduledDockDoorRecordStatus.EXPIRED -> return true
                }
                return false
            }
            ScheduledDockDoorRecordStatus.GRANTED -> {
                when (next) {
                    ScheduledDockDoorRecordStatus.REVOKED, ScheduledDockDoorRecordStatus.EXPIRED -> return true
                }
                return false
            }
            ScheduledDockDoorRecordStatus.REVOKED -> {
                when (next) {
                    ScheduledDockDoorRecordStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledDockDoorRecordStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dock door record $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled dock door record"} $code"

    fun adjustedExposure(): Double = round((odometer.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledDockDoorRecord {
        return ScheduledDockDoorRecord(
            code = code,
            name = name,
            score = score,
            endsAt = endsAt,
            notes = notes,
            floorValue = floorValue,
            odometer = odometer,
            expiresAt = expiresAt,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled dock door record in status requested. */
fun newScheduledDockDoorRecord(code: String, name: String): ScheduledDockDoorRecord {
    return ScheduledDockDoorRecord(
        code = code,
        name = name,
        score = 0.0,
        endsAt = 0,
        notes = "",
        floorValue = 0.0,
        odometer = 0,
        expiresAt = 0,
        description = "",
        status = ScheduledDockDoorRecordStatus.REQUESTED,
    )
}
