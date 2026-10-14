package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone baseline is in its life cycle. */
enum class ZoneBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a zone baseline and its current status. */
data class ZoneBaseline(
    var code: String,
    var label: String,
    var loadKg: Double,
    var floorValue: Double,
    var currency: String,
    var graceDays: Int,
    var active: Boolean,
    var status: ZoneBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneBaselineStatus): Boolean {
        when (status) {
            ZoneBaselineStatus.DRAFT -> return next == ZoneBaselineStatus.CLOSED
            ZoneBaselineStatus.PENDING -> return next == ZoneBaselineStatus.SUSPENDED || next == ZoneBaselineStatus.CLOSED
            ZoneBaselineStatus.ACTIVE -> return next == ZoneBaselineStatus.CLOSED
            ZoneBaselineStatus.SUSPENDED -> {
                when (next) {
                    ZoneBaselineStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneBaselineStatus) {
        if (!canMoveTo(next)) {
            error("zone baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"zone baseline"} $code"

    fun netVolume(): Double = round((floorValue + loadKg) * 100) / 100

    fun snapshot(): ZoneBaseline {
        return ZoneBaseline(
            code = code,
            label = label,
            loadKg = loadKg,
            floorValue = floorValue,
            currency = currency,
            graceDays = graceDays,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a zone baseline in status draft. */
fun newZoneBaseline(code: String, label: String): ZoneBaseline {
    return ZoneBaseline(
        code = code,
        label = label,
        loadKg = 0.0,
        floorValue = 0.0,
        currency = "",
        graceDays = 0,
        active = false,
        status = ZoneBaselineStatus.DRAFT,
    )
}
