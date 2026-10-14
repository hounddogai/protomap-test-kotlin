package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy sortation manifest records of terminal's sortation work, and the rules, stores, and calculations
// around them.

/** Where a legacy sortation manifest is in its life cycle. */
enum class LegacySortationManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a legacy sortation manifest and its current status. */
data class LegacySortationManifest(
    var code: String,
    var name: String,
    var cost: Double,
    var expiresAt: Int,
    var attempts: Int,
    var fuelLiters: Double,
    var notes: String,
    var timezone: String,
    var phone: String,
    var status: LegacySortationManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacySortationManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacySortationManifestStatus): Boolean {
        when (status) {
            LegacySortationManifestStatus.PLANNED -> {
                when (next) {
                    LegacySortationManifestStatus.SCHEDULED -> return true
                }
                return false
            }
            LegacySortationManifestStatus.SCHEDULED -> return next == LegacySortationManifestStatus.CANCELLED
            LegacySortationManifestStatus.IN_PROGRESS -> return next == LegacySortationManifestStatus.COMPLETED
            LegacySortationManifestStatus.COMPLETED -> {
                when (next) {
                    LegacySortationManifestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacySortationManifestStatus) {
        if (!canMoveTo(next)) {
            error("legacy sortation manifest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round((cost + fuelLiters) * 100) / 100

    fun snapshot(): LegacySortationManifest {
        return LegacySortationManifest(
            code = code,
            name = name,
            cost = cost,
            expiresAt = expiresAt,
            attempts = attempts,
            fuelLiters = fuelLiters,
            notes = notes,
            timezone = timezone,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy sortation manifest in status planned. */
fun newLegacySortationManifest(code: String, name: String): LegacySortationManifest {
    return LegacySortationManifest(
        code = code,
        name = name,
        cost = 0.0,
        expiresAt = 0,
        attempts = 0,
        fuelLiters = 0.0,
        notes = "",
        timezone = "",
        phone = "",
        status = LegacySortationManifestStatus.PLANNED,
    )
}
