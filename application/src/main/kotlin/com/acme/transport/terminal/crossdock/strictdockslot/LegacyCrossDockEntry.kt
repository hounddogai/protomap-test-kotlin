package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy cross dock entry is in its life cycle. */
enum class LegacyCrossDockEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A legacy cross dock entry as the cross dock team keeps it. */
data class LegacyCrossDockEntry(
    var code: String,
    var label: String,
    var budget: Double,
    var dock: String,
    var endsAt: Int,
    var fuelLiters: Double,
    var driverId: String,
    var status: LegacyCrossDockEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCrossDockEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCrossDockEntryStatus): Boolean {
        when (status) {
            LegacyCrossDockEntryStatus.OPEN -> {
                when (next) {
                    LegacyCrossDockEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyCrossDockEntryStatus.IN_REVIEW -> {
                when (next) {
                    LegacyCrossDockEntryStatus.REOPENED, LegacyCrossDockEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyCrossDockEntryStatus.RESOLVED -> {
                when (next) {
                    LegacyCrossDockEntryStatus.REOPENED, LegacyCrossDockEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyCrossDockEntryStatus.REOPENED -> return next == LegacyCrossDockEntryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LegacyCrossDockEntryStatus) {
        if (!canMoveTo(next)) {
            error("legacy cross dock entry $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedTotal(): Double = round((fuelLiters + endsAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyCrossDockEntry {
        return LegacyCrossDockEntry(
            code = code,
            label = label,
            budget = budget,
            dock = dock,
            endsAt = endsAt,
            fuelLiters = fuelLiters,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy cross dock entry in status open. */
fun newLegacyCrossDockEntry(code: String, label: String): LegacyCrossDockEntry {
    return LegacyCrossDockEntry(
        code = code,
        label = label,
        budget = 0.0,
        dock = "",
        endsAt = 0,
        fuelLiters = 0.0,
        driverId = "",
        status = LegacyCrossDockEntryStatus.OPEN,
    )
}
