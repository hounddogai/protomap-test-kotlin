package com.acme.transport.linehaul.trailer.trailerpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional trailer preference records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a regional trailer preference is in its life cycle. */
enum class RegionalTrailerPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A regional trailer preference. Its code is unique within a tenant. */
data class RegionalTrailerPreference(
    var code: String,
    var title: String,
    var pallets: Int,
    var attempts: Int,
    var vehicleId: String,
    var region: String,
    var sequence: Int,
    var checksum: String,
    var status: RegionalTrailerPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTrailerPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTrailerPreferenceStatus): Boolean {
        when (status) {
            RegionalTrailerPreferenceStatus.PLANNED -> {
                when (next) {
                    RegionalTrailerPreferenceStatus.SCHEDULED, RegionalTrailerPreferenceStatus.COMPLETED -> return true
                }
                return false
            }
            RegionalTrailerPreferenceStatus.SCHEDULED -> {
                when (next) {
                    RegionalTrailerPreferenceStatus.IN_PROGRESS, RegionalTrailerPreferenceStatus.COMPLETED -> return true
                }
                return false
            }
            RegionalTrailerPreferenceStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalTrailerPreferenceStatus.COMPLETED, RegionalTrailerPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalTrailerPreferenceStatus.COMPLETED -> return next == RegionalTrailerPreferenceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RegionalTrailerPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("regional trailer preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netValue(): Double = round((pallets.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): RegionalTrailerPreference {
        return RegionalTrailerPreference(
            code = code,
            title = title,
            pallets = pallets,
            attempts = attempts,
            vehicleId = vehicleId,
            region = region,
            sequence = sequence,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a regional trailer preference in status planned. */
fun newRegionalTrailerPreference(code: String, title: String): RegionalTrailerPreference {
    return RegionalTrailerPreference(
        code = code,
        title = title,
        pallets = 0,
        attempts = 0,
        vehicleId = "",
        region = "",
        sequence = 0,
        checksum = "",
        status = RegionalTrailerPreferenceStatus.PLANNED,
    )
}
