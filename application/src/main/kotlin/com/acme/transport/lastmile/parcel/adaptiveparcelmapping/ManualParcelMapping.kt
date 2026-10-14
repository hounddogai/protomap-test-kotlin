package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual parcel mapping records of lastmile's parcel work, and the rules, stores, and calculations around
// them.

/** Where a manual parcel mapping is in its life cycle. */
enum class ManualParcelMappingStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual parcel mapping. Its code is unique within a tenant. */
data class ManualParcelMapping(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var windowDays: Int,
    var approved: Boolean,
    var country: String,
    var trailerId: String,
    var checksum: String,
    var locale: String,
    var summary: String,
    var status: ManualParcelMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualParcelMappingStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualParcelMappingStatus): Boolean {
        when (status) {
            ManualParcelMappingStatus.PROPOSED -> {
                when (next) {
                    ManualParcelMappingStatus.APPROVED, ManualParcelMappingStatus.REJECTED -> return true
                }
                return false
            }
            ManualParcelMappingStatus.APPROVED -> return next == ManualParcelMappingStatus.REJECTED || next == ManualParcelMappingStatus.WITHDRAWN
            ManualParcelMappingStatus.REJECTED -> {
                when (next) {
                    ManualParcelMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualParcelMappingStatus) {
        if (!canMoveTo(next)) {
            error("manual parcel mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual parcel mapping"} $code"

    fun effectiveValue(): Double = round((windowDays.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): ManualParcelMapping {
        return ManualParcelMapping(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            windowDays = windowDays,
            approved = approved,
            country = country,
            trailerId = trailerId,
            checksum = checksum,
            locale = locale,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a manual parcel mapping in status proposed. */
fun newManualParcelMapping(code: String, name: String): ManualParcelMapping {
    return ManualParcelMapping(
        code = code,
        name = name,
        durationMinutes = 0,
        windowDays = 0,
        approved = false,
        country = "",
        trailerId = "",
        checksum = "",
        locale = "",
        summary = "",
        status = ManualParcelMappingStatus.PROPOSED,
    )
}
