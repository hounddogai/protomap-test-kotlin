package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped zone baseline records of network's zone work, and the rules, stores, and calculations around
// them.

/** Where a capped zone baseline is in its life cycle. */
enum class CappedZoneBaselineStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped zone baseline. Its code is unique within a tenant. */
data class CappedZoneBaseline(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var name: String,
    var capacity: Int,
    var approved: Boolean,
    var status: CappedZoneBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedZoneBaselineStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedZoneBaselineStatus): Boolean {
        when (status) {
            CappedZoneBaselineStatus.PROPOSED -> return next == CappedZoneBaselineStatus.APPROVED || next == CappedZoneBaselineStatus.WITHDRAWN
            CappedZoneBaselineStatus.APPROVED -> {
                when (next) {
                    CappedZoneBaselineStatus.REJECTED -> return true
                }
                return false
            }
            CappedZoneBaselineStatus.REJECTED -> return next == CappedZoneBaselineStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedZoneBaselineStatus) {
        if (!canMoveTo(next)) {
            error("capped zone baseline $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netLoad(): Double = round(capacity.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): CappedZoneBaseline {
        return CappedZoneBaseline(
            code = code,
            title = title,
            expiresAt = expiresAt,
            name = name,
            capacity = capacity,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped zone baseline in status proposed. */
fun newCappedZoneBaseline(code: String, title: String): CappedZoneBaseline {
    return CappedZoneBaseline(
        code = code,
        title = title,
        expiresAt = 0,
        name = "",
        capacity = 0,
        approved = false,
        status = CappedZoneBaselineStatus.PROPOSED,
    )
}
