package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard entry is in its life cycle. */
enum class ScorecardEntryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scorecard entry as the scorecard team keeps it. */
data class ScorecardEntry(
    var code: String,
    var name: String,
    var discount: Double,
    var region: String,
    var locale: String,
    var etaMinutes: Int,
    var stopCount: Int,
    var status: ScorecardEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardEntryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardEntryStatus): Boolean {
        when (status) {
            ScorecardEntryStatus.PLANNED -> return next == ScorecardEntryStatus.IN_PROGRESS || next == ScorecardEntryStatus.CANCELLED
            ScorecardEntryStatus.SCHEDULED -> return next == ScorecardEntryStatus.IN_PROGRESS || next == ScorecardEntryStatus.COMPLETED
            ScorecardEntryStatus.IN_PROGRESS -> return next == ScorecardEntryStatus.COMPLETED
            ScorecardEntryStatus.COMPLETED -> {
                when (next) {
                    ScorecardEntryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardEntryStatus) {
        if (!canMoveTo(next)) {
            error("scorecard entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netVolume(): Double = round(etaMinutes.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): ScorecardEntry {
        return ScorecardEntry(
            code = code,
            name = name,
            discount = discount,
            region = region,
            locale = locale,
            etaMinutes = etaMinutes,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scorecard entry in status planned. */
fun newScorecardEntry(code: String, name: String): ScorecardEntry {
    return ScorecardEntry(
        code = code,
        name = name,
        discount = 0.0,
        region = "",
        locale = "",
        etaMinutes = 0,
        stopCount = 0,
        status = ScorecardEntryStatus.PLANNED,
    )
}
