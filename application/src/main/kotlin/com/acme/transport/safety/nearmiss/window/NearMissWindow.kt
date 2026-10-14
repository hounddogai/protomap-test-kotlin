package com.acme.transport.safety.nearmiss.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The near miss window records of safety's near miss work, and the rules, stores, and calculations around
// them.

/** Where a near miss window is in its life cycle. */
enum class NearMissWindowStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A near miss window as the near miss team keeps it. */
data class NearMissWindow(
    var code: String,
    var title: String,
    var approved: Boolean,
    var active: Boolean,
    var amount: Double,
    var rate: Double,
    var status: NearMissWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissWindowStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissWindowStatus): Boolean {
        when (status) {
            NearMissWindowStatus.REQUESTED -> return next == NearMissWindowStatus.EXPIRED
            NearMissWindowStatus.GRANTED -> {
                when (next) {
                    NearMissWindowStatus.REVOKED, NearMissWindowStatus.EXPIRED -> return true
                }
                return false
            }
            NearMissWindowStatus.REVOKED -> return next == NearMissWindowStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: NearMissWindowStatus) {
        if (!canMoveTo(next)) {
            error("near miss window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedTotal(): Double = round(amount * rate * 100) / 100

    fun snapshot(): NearMissWindow {
        return NearMissWindow(
            code = code,
            title = title,
            approved = approved,
            active = active,
            amount = amount,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a near miss window in status requested. */
fun newNearMissWindow(code: String, title: String): NearMissWindow {
    return NearMissWindow(
        code = code,
        title = title,
        approved = false,
        active = false,
        amount = 0.0,
        rate = 0.0,
        status = NearMissWindowStatus.REQUESTED,
    )
}
