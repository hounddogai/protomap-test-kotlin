package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped idling target records of emissions's idling work, and the rules, stores, and calculations around
// them.

/** Where a capped idling target is in its life cycle. */
enum class CappedIdlingTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped idling target as the idling team keeps it. */
data class CappedIdlingTarget(
    var code: String,
    var label: String,
    var timezone: String,
    var createdAt: Int,
    var approved: Boolean,
    var region: String,
    var loadKg: Double,
    var expiresAt: Int,
    var status: CappedIdlingTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedIdlingTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedIdlingTargetStatus): Boolean {
        when (status) {
            CappedIdlingTargetStatus.PROPOSED -> return next == CappedIdlingTargetStatus.APPROVED || next == CappedIdlingTargetStatus.WITHDRAWN
            CappedIdlingTargetStatus.APPROVED -> {
                when (next) {
                    CappedIdlingTargetStatus.REJECTED -> return true
                }
                return false
            }
            CappedIdlingTargetStatus.REJECTED -> return next == CappedIdlingTargetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedIdlingTargetStatus) {
        if (!canMoveTo(next)) {
            error("capped idling target $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedVolume(): Double = round((loadKg + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): CappedIdlingTarget {
        return CappedIdlingTarget(
            code = code,
            label = label,
            timezone = timezone,
            createdAt = createdAt,
            approved = approved,
            region = region,
            loadKg = loadKg,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capped idling target in status proposed. */
fun newCappedIdlingTarget(code: String, label: String): CappedIdlingTarget {
    return CappedIdlingTarget(
        code = code,
        label = label,
        timezone = "",
        createdAt = 0,
        approved = false,
        region = "",
        loadKg = 0.0,
        expiresAt = 0,
        status = CappedIdlingTargetStatus.PROPOSED,
    )
}
