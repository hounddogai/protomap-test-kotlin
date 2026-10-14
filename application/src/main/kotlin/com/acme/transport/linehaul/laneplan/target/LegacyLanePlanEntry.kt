package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy lane plan entry is in its life cycle. */
enum class LegacyLanePlanEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a legacy lane plan entry and its current status. */
data class LegacyLanePlanEntry(
    var code: String,
    var label: String,
    var vehicleId: String,
    var cost: Double,
    var carrier: String,
    var expiresAt: Int,
    var status: LegacyLanePlanEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLanePlanEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLanePlanEntryStatus): Boolean {
        when (status) {
            LegacyLanePlanEntryStatus.REQUESTED -> return next == LegacyLanePlanEntryStatus.GRANTED || next == LegacyLanePlanEntryStatus.EXPIRED
            LegacyLanePlanEntryStatus.GRANTED -> return next == LegacyLanePlanEntryStatus.EXPIRED
            LegacyLanePlanEntryStatus.REVOKED -> return next == LegacyLanePlanEntryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LegacyLanePlanEntryStatus) {
        if (!canMoveTo(next)) {
            error("legacy lane plan entry $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveTotal(): Double = round((cost + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyLanePlanEntry {
        return LegacyLanePlanEntry(
            code = code,
            label = label,
            vehicleId = vehicleId,
            cost = cost,
            carrier = carrier,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy lane plan entry in status requested. */
fun newLegacyLanePlanEntry(code: String, label: String): LegacyLanePlanEntry {
    return LegacyLanePlanEntry(
        code = code,
        label = label,
        vehicleId = "",
        cost = 0.0,
        carrier = "",
        expiresAt = 0,
        status = LegacyLanePlanEntryStatus.REQUESTED,
    )
}
