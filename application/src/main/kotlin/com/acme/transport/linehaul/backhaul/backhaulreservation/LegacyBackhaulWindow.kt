package com.acme.transport.linehaul.backhaul.backhaulreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy backhaul window is in its life cycle. */
enum class LegacyBackhaulWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One legacy backhaul window, with what the planners need to act on it. */
data class LegacyBackhaulWindow(
    var code: String,
    var title: String,
    var notes: String,
    var pinned: Boolean,
    var ceiling: Double,
    var minimum: Double,
    var retries: Int,
    var status: LegacyBackhaulWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyBackhaulWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyBackhaulWindowStatus): Boolean {
        when (status) {
            LegacyBackhaulWindowStatus.OPEN -> return next == LegacyBackhaulWindowStatus.RESOLVED || next == LegacyBackhaulWindowStatus.ARCHIVED
            LegacyBackhaulWindowStatus.IN_REVIEW -> return next == LegacyBackhaulWindowStatus.REOPENED
            LegacyBackhaulWindowStatus.RESOLVED -> return next == LegacyBackhaulWindowStatus.REOPENED || next == LegacyBackhaulWindowStatus.ARCHIVED
            LegacyBackhaulWindowStatus.REOPENED -> {
                when (next) {
                    LegacyBackhaulWindowStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyBackhaulWindowStatus) {
        if (!canMoveTo(next)) {
            error("legacy backhaul window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy backhaul window"} $code"

    fun projectedTotal(): Double = round(minimum * ceiling * 100) / 100

    fun snapshot(): LegacyBackhaulWindow {
        return LegacyBackhaulWindow(
            code = code,
            title = title,
            notes = notes,
            pinned = pinned,
            ceiling = ceiling,
            minimum = minimum,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy backhaul window in status open. */
fun newLegacyBackhaulWindow(code: String, title: String): LegacyBackhaulWindow {
    return LegacyBackhaulWindow(
        code = code,
        title = title,
        notes = "",
        pinned = false,
        ceiling = 0.0,
        minimum = 0.0,
        retries = 0,
        status = LegacyBackhaulWindowStatus.OPEN,
    )
}
