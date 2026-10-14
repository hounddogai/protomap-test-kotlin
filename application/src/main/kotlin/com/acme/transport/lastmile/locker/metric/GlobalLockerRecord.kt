package com.acme.transport.lastmile.locker.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global locker record is in its life cycle. */
enum class GlobalLockerRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a global locker record and its current status. */
data class GlobalLockerRecord(
    var code: String,
    var name: String,
    var budget: Double,
    var durationMinutes: Int,
    var segment: String,
    var fuelLiters: Double,
    var windowDays: Int,
    var status: GlobalLockerRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalLockerRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalLockerRecordStatus): Boolean {
        when (status) {
            GlobalLockerRecordStatus.REQUESTED -> return next == GlobalLockerRecordStatus.GRANTED || next == GlobalLockerRecordStatus.REVOKED
            GlobalLockerRecordStatus.GRANTED -> return next == GlobalLockerRecordStatus.EXPIRED
            GlobalLockerRecordStatus.REVOKED -> return next == GlobalLockerRecordStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalLockerRecordStatus) {
        if (!canMoveTo(next)) {
            error("global locker record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round((durationMinutes.toDouble() - budget) * 100) / 100

    fun snapshot(): GlobalLockerRecord {
        return GlobalLockerRecord(
            code = code,
            name = name,
            budget = budget,
            durationMinutes = durationMinutes,
            segment = segment,
            fuelLiters = fuelLiters,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a global locker record in status requested. */
fun newGlobalLockerRecord(code: String, name: String): GlobalLockerRecord {
    return GlobalLockerRecord(
        code = code,
        name = name,
        budget = 0.0,
        durationMinutes = 0,
        segment = "",
        fuelLiters = 0.0,
        windowDays = 0,
        status = GlobalLockerRecordStatus.REQUESTED,
    )
}
