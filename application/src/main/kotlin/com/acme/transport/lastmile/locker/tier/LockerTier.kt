package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.text.normalize

// The locker tier records of lastmile's locker work, and the rules, stores, and calculations around them.

/** Where a locker tier is in its life cycle. */
enum class LockerTierStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A locker tier. Its code is unique within a tenant. */
data class LockerTier(
    var code: String,
    var title: String,
    var summary: String,
    var loadKg: Double,
    var attributes: MutableMap<String, String>,
    var notes: String,
    var channel: String,
    var status: LockerTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerTierStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerTierStatus): Boolean {
        when (status) {
            LockerTierStatus.DRAFT -> {
                when (next) {
                    LockerTierStatus.PENDING -> return true
                }
                return false
            }
            LockerTierStatus.PENDING -> return next == LockerTierStatus.SUSPENDED || next == LockerTierStatus.CLOSED
            LockerTierStatus.ACTIVE -> return next == LockerTierStatus.SUSPENDED
            LockerTierStatus.SUSPENDED -> return next == LockerTierStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LockerTierStatus) {
        if (!canMoveTo(next)) {
            error("locker tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = loadKg

    fun snapshot(): LockerTier {
        return LockerTier(
            code = code,
            title = title,
            summary = summary,
            loadKg = loadKg,
            attributes = attributes,
            notes = notes,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a locker tier in status draft. */
fun newLockerTier(code: String, title: String): LockerTier {
    return LockerTier(
        code = code,
        title = title,
        summary = "",
        loadKg = 0.0,
        attributes = mutableMapOf<String, String>(),
        notes = "",
        channel = "",
        status = LockerTierStatus.DRAFT,
    )
}
