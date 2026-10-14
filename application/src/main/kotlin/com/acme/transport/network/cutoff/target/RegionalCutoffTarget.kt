package com.acme.transport.network.cutoff.target

import com.acme.transport.kit.text.normalize

// The regional cutoff target records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a regional cutoff target is in its life cycle. */
enum class RegionalCutoffTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a regional cutoff target and its current status. */
data class RegionalCutoffTarget(
    var code: String,
    var name: String,
    var unit: String,
    var ownerId: String,
    var title: String,
    var active: Boolean,
    var timezone: String,
    var createdAt: Int,
    var status: RegionalCutoffTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCutoffTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCutoffTargetStatus): Boolean {
        when (status) {
            RegionalCutoffTargetStatus.PROPOSED -> return next == RegionalCutoffTargetStatus.REJECTED
            RegionalCutoffTargetStatus.APPROVED -> {
                when (next) {
                    RegionalCutoffTargetStatus.REJECTED -> return true
                }
                return false
            }
            RegionalCutoffTargetStatus.REJECTED -> return next == RegionalCutoffTargetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RegionalCutoffTargetStatus) {
        if (!canMoveTo(next)) {
            error("regional cutoff target $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): RegionalCutoffTarget {
        return RegionalCutoffTarget(
            code = code,
            name = name,
            unit = unit,
            ownerId = ownerId,
            title = title,
            active = active,
            timezone = timezone,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional cutoff target in status proposed. */
fun newRegionalCutoffTarget(code: String, name: String): RegionalCutoffTarget {
    return RegionalCutoffTarget(
        code = code,
        name = name,
        unit = "",
        ownerId = "",
        title = "",
        active = false,
        timezone = "",
        createdAt = 0,
        status = RegionalCutoffTargetStatus.PROPOSED,
    )
}
