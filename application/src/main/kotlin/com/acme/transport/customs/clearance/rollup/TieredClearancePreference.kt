package com.acme.transport.customs.clearance.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered clearance preference is in its life cycle. */
enum class TieredClearancePreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered clearance preference. Its code is unique within a tenant. */
data class TieredClearancePreference(
    var code: String,
    var label: String,
    var priority: Int,
    var startsAt: Int,
    var endsAt: Int,
    var capacity: Int,
    var region: String,
    var trailerId: String,
    var expiresAt: Int,
    var name: String,
    var limit: Int,
    var status: TieredClearancePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredClearancePreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredClearancePreferenceStatus): Boolean {
        when (status) {
            TieredClearancePreferenceStatus.PROPOSED -> return next == TieredClearancePreferenceStatus.APPROVED
            TieredClearancePreferenceStatus.APPROVED -> {
                when (next) {
                    TieredClearancePreferenceStatus.REJECTED -> return true
                }
                return false
            }
            TieredClearancePreferenceStatus.REJECTED -> {
                when (next) {
                    TieredClearancePreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredClearancePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("tiered clearance preference $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedValue(): Double = round((priority.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): TieredClearancePreference {
        return TieredClearancePreference(
            code = code,
            label = label,
            priority = priority,
            startsAt = startsAt,
            endsAt = endsAt,
            capacity = capacity,
            region = region,
            trailerId = trailerId,
            expiresAt = expiresAt,
            name = name,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tiered clearance preference in status proposed. */
fun newTieredClearancePreference(code: String, label: String): TieredClearancePreference {
    return TieredClearancePreference(
        code = code,
        label = label,
        priority = 0,
        startsAt = 0,
        endsAt = 0,
        capacity = 0,
        region = "",
        trailerId = "",
        expiresAt = 0,
        name = "",
        limit = 0,
        status = TieredClearancePreferenceStatus.PROPOSED,
    )
}
