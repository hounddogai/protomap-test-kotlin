package com.acme.transport.emissions.evcharge.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped ev charge record records of emissions's ev charge work, and the rules, stores, and calculations
// around them.

/** Where a capped ev charge record is in its life cycle. */
enum class CappedEvChargeRecordStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One capped ev charge record, with what the planners need to act on it. */
data class CappedEvChargeRecord(
    var code: String,
    var label: String,
    var vehicleId: String,
    var routeCode: String,
    var endsAt: Int,
    var lane: String,
    var timezone: String,
    var source: String,
    var expiresAt: Int,
    var status: CappedEvChargeRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedEvChargeRecordStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedEvChargeRecordStatus): Boolean {
        when (status) {
            CappedEvChargeRecordStatus.DRAFT -> {
                when (next) {
                    CappedEvChargeRecordStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedEvChargeRecordStatus.PENDING -> return next == CappedEvChargeRecordStatus.ACTIVE
            CappedEvChargeRecordStatus.ACTIVE -> return next == CappedEvChargeRecordStatus.CLOSED
            CappedEvChargeRecordStatus.SUSPENDED -> {
                when (next) {
                    CappedEvChargeRecordStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedEvChargeRecordStatus) {
        if (!canMoveTo(next)) {
            error("capped ev charge record $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"capped ev charge record"} $code"

    fun adjustedLoad(): Double = round(expiresAt.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): CappedEvChargeRecord {
        return CappedEvChargeRecord(
            code = code,
            label = label,
            vehicleId = vehicleId,
            routeCode = routeCode,
            endsAt = endsAt,
            lane = lane,
            timezone = timezone,
            source = source,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capped ev charge record in status draft. */
fun newCappedEvChargeRecord(code: String, label: String): CappedEvChargeRecord {
    return CappedEvChargeRecord(
        code = code,
        label = label,
        vehicleId = "",
        routeCode = "",
        endsAt = 0,
        lane = "",
        timezone = "",
        source = "",
        expiresAt = 0,
        status = CappedEvChargeRecordStatus.DRAFT,
    )
}
