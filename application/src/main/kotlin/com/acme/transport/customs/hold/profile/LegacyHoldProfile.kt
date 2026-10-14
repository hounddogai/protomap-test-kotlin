package com.acme.transport.customs.hold.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy hold profile records of customs's hold work, and the rules, stores, and calculations around
// them.

/** Where a legacy hold profile is in its life cycle. */
enum class LegacyHoldProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a legacy hold profile and its current status. */
data class LegacyHoldProfile(
    var code: String,
    var name: String,
    var label: String,
    var pinned: Boolean,
    var tags: MutableList<String>,
    var version: Int,
    var distanceKm: Double,
    var unit: String,
    var status: LegacyHoldProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyHoldProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyHoldProfileStatus): Boolean {
        when (status) {
            LegacyHoldProfileStatus.DRAFT -> return next == LegacyHoldProfileStatus.ACTIVE
            LegacyHoldProfileStatus.PENDING -> return next == LegacyHoldProfileStatus.CLOSED
            LegacyHoldProfileStatus.ACTIVE -> return next == LegacyHoldProfileStatus.SUSPENDED
            LegacyHoldProfileStatus.SUSPENDED -> return next == LegacyHoldProfileStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LegacyHoldProfileStatus) {
        if (!canMoveTo(next)) {
            error("legacy hold profile $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedValue(): Double = round((distanceKm - version.toDouble()) * 100) / 100

    fun snapshot(): LegacyHoldProfile {
        return LegacyHoldProfile(
            code = code,
            name = name,
            label = label,
            pinned = pinned,
            tags = tags,
            version = version,
            distanceKm = distanceKm,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a legacy hold profile in status draft. */
fun newLegacyHoldProfile(code: String, name: String): LegacyHoldProfile {
    return LegacyHoldProfile(
        code = code,
        name = name,
        label = "",
        pinned = false,
        tags = mutableListOf<String>(),
        version = 0,
        distanceKm = 0.0,
        unit = "",
        status = LegacyHoldProfileStatus.DRAFT,
    )
}
