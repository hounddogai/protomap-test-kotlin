package com.acme.transport.fleetops.tire.tirewindow

import com.acme.transport.kit.text.normalize

// The tiered tire window records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a tiered tire window is in its life cycle. */
enum class TieredTireWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered tire window as the tire team keeps it. */
data class TieredTireWindow(
    var code: String,
    var label: String,
    var approved: Boolean,
    var contactEmail: String,
    var country: String,
    var score: Double,
    var status: TieredTireWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTireWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTireWindowStatus): Boolean {
        when (status) {
            TieredTireWindowStatus.PROPOSED -> return next == TieredTireWindowStatus.APPROVED || next == TieredTireWindowStatus.WITHDRAWN
            TieredTireWindowStatus.APPROVED -> {
                when (next) {
                    TieredTireWindowStatus.REJECTED, TieredTireWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredTireWindowStatus.REJECTED -> {
                when (next) {
                    TieredTireWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredTireWindowStatus) {
        if (!canMoveTo(next)) {
            error("tiered tire window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = score

    fun snapshot(): TieredTireWindow {
        return TieredTireWindow(
            code = code,
            label = label,
            approved = approved,
            contactEmail = contactEmail,
            country = country,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered tire window in status proposed. */
fun newTieredTireWindow(code: String, label: String): TieredTireWindow {
    return TieredTireWindow(
        code = code,
        label = label,
        approved = false,
        contactEmail = "",
        country = "",
        score = 0.0,
        status = TieredTireWindowStatus.PROPOSED,
    )
}
