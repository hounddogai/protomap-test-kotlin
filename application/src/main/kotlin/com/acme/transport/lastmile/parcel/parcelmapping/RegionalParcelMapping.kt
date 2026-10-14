package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.text.normalize

// The regional parcel mapping records of lastmile's parcel work, and the rules, stores, and calculations
// around them.

/** Where a regional parcel mapping is in its life cycle. */
enum class RegionalParcelMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A regional parcel mapping. Its code is unique within a tenant. */
data class RegionalParcelMapping(
    var code: String,
    var name: String,
    var region: String,
    var notes: String,
    var startsAt: Int,
    var trailerId: String,
    var status: RegionalParcelMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalParcelMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalParcelMappingStatus): Boolean {
        when (status) {
            RegionalParcelMappingStatus.OPEN -> return next == RegionalParcelMappingStatus.IN_REVIEW || next == RegionalParcelMappingStatus.ARCHIVED
            RegionalParcelMappingStatus.IN_REVIEW -> return next == RegionalParcelMappingStatus.RESOLVED || next == RegionalParcelMappingStatus.REOPENED
            RegionalParcelMappingStatus.RESOLVED -> {
                when (next) {
                    RegionalParcelMappingStatus.REOPENED, RegionalParcelMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalParcelMappingStatus.REOPENED -> {
                when (next) {
                    RegionalParcelMappingStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalParcelMappingStatus) {
        if (!canMoveTo(next)) {
            error("regional parcel mapping $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): RegionalParcelMapping {
        return RegionalParcelMapping(
            code = code,
            name = name,
            region = region,
            notes = notes,
            startsAt = startsAt,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a regional parcel mapping in status open. */
fun newRegionalParcelMapping(code: String, name: String): RegionalParcelMapping {
    return RegionalParcelMapping(
        code = code,
        name = name,
        region = "",
        notes = "",
        startsAt = 0,
        trailerId = "",
        status = RegionalParcelMappingStatus.OPEN,
    )
}
