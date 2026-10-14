package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict roster manifest is in its life cycle. */
enum class StrictRosterManifestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a strict roster manifest and its current status. */
data class StrictRosterManifest(
    var code: String,
    var label: String,
    var version: Int,
    var country: String,
    var quantity: Int,
    var status: StrictRosterManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRosterManifestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRosterManifestStatus): Boolean {
        when (status) {
            StrictRosterManifestStatus.REQUESTED -> {
                when (next) {
                    StrictRosterManifestStatus.REVOKED, StrictRosterManifestStatus.EXPIRED -> return true
                }
                return false
            }
            StrictRosterManifestStatus.GRANTED -> return next == StrictRosterManifestStatus.REVOKED || next == StrictRosterManifestStatus.EXPIRED
            StrictRosterManifestStatus.REVOKED -> {
                when (next) {
                    StrictRosterManifestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictRosterManifestStatus) {
        if (!canMoveTo(next)) {
            error("strict roster manifest $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round((quantity.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): StrictRosterManifest {
        return StrictRosterManifest(
            code = code,
            label = label,
            version = version,
            country = country,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict roster manifest in status requested. */
fun newStrictRosterManifest(code: String, label: String): StrictRosterManifest {
    return StrictRosterManifest(
        code = code,
        label = label,
        version = 0,
        country = "",
        quantity = 0,
        status = StrictRosterManifestStatus.REQUESTED,
    )
}
