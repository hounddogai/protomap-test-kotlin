package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional rest break entry is in its life cycle. */
enum class RegionalRestBreakEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A regional rest break entry. Its code is unique within a tenant. */
data class RegionalRestBreakEntry(
    var code: String,
    var name: String,
    var attempts: Int,
    var endsAt: Int,
    var visible: Boolean,
    var etaMinutes: Int,
    var pinned: Boolean,
    var vehicleId: String,
    var lane: String,
    var maximum: Double,
    var status: RegionalRestBreakEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRestBreakEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRestBreakEntryStatus): Boolean {
        when (status) {
            RegionalRestBreakEntryStatus.REQUESTED -> return next == RegionalRestBreakEntryStatus.REVOKED || next == RegionalRestBreakEntryStatus.EXPIRED
            RegionalRestBreakEntryStatus.GRANTED -> return next == RegionalRestBreakEntryStatus.REVOKED || next == RegionalRestBreakEntryStatus.EXPIRED
            RegionalRestBreakEntryStatus.REVOKED -> return next == RegionalRestBreakEntryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalRestBreakEntryStatus) {
        if (!canMoveTo(next)) {
            error("regional rest break entry $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional rest break entry"} $code"

    fun netValue(): Double = round((attempts.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): RegionalRestBreakEntry {
        return RegionalRestBreakEntry(
            code = code,
            name = name,
            attempts = attempts,
            endsAt = endsAt,
            visible = visible,
            etaMinutes = etaMinutes,
            pinned = pinned,
            vehicleId = vehicleId,
            lane = lane,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a regional rest break entry in status requested. */
fun newRegionalRestBreakEntry(code: String, name: String): RegionalRestBreakEntry {
    return RegionalRestBreakEntry(
        code = code,
        name = name,
        attempts = 0,
        endsAt = 0,
        visible = false,
        etaMinutes = 0,
        pinned = false,
        vehicleId = "",
        lane = "",
        maximum = 0.0,
        status = RegionalRestBreakEntryStatus.REQUESTED,
    )
}
