package com.acme.transport.customs.clearance.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The clearance preference records of customs's clearance work, and the rules, stores, and calculations
// around them.

/** Where a clearance preference is in its life cycle. */
enum class ClearancePreferenceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A clearance preference as the clearance team keeps it. */
data class ClearancePreference(
    var code: String,
    var name: String,
    var endsAt: Int,
    var limit: Int,
    var region: String,
    var tags: MutableList<String>,
    var locale: String,
    var capacity: Int,
    var description: String,
    var updatedAt: Int,
    var status: ClearancePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearancePreferenceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearancePreferenceStatus): Boolean {
        when (status) {
            ClearancePreferenceStatus.CREATED -> {
                when (next) {
                    ClearancePreferenceStatus.RETIRED -> return true
                }
                return false
            }
            ClearancePreferenceStatus.VALIDATED -> return next == ClearancePreferenceStatus.PUBLISHED || next == ClearancePreferenceStatus.RETIRED
            ClearancePreferenceStatus.PUBLISHED -> {
                when (next) {
                    ClearancePreferenceStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearancePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("clearance preference $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"clearance preference"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round((updatedAt.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): ClearancePreference {
        return ClearancePreference(
            code = code,
            name = name,
            endsAt = endsAt,
            limit = limit,
            region = region,
            tags = tags,
            locale = locale,
            capacity = capacity,
            description = description,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a clearance preference in status created. */
fun newClearancePreference(code: String, name: String): ClearancePreference {
    return ClearancePreference(
        code = code,
        name = name,
        endsAt = 0,
        limit = 0,
        region = "",
        tags = mutableListOf<String>(),
        locale = "",
        capacity = 0,
        description = "",
        updatedAt = 0,
        status = ClearancePreferenceStatus.CREATED,
    )
}
