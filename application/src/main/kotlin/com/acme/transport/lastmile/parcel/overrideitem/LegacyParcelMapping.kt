package com.acme.transport.lastmile.parcel.overrideitem

import com.acme.transport.kit.text.normalize

/** Where a legacy parcel mapping is in its life cycle. */
enum class LegacyParcelMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a legacy parcel mapping and its current status. */
data class LegacyParcelMapping(
    var code: String,
    var label: String,
    var timezone: String,
    var shiftCode: String,
    var priority: Int,
    var depot: String,
    var status: LegacyParcelMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyParcelMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyParcelMappingStatus): Boolean {
        when (status) {
            LegacyParcelMappingStatus.REQUESTED -> return next == LegacyParcelMappingStatus.GRANTED || next == LegacyParcelMappingStatus.REVOKED
            LegacyParcelMappingStatus.GRANTED -> {
                when (next) {
                    LegacyParcelMappingStatus.REVOKED, LegacyParcelMappingStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyParcelMappingStatus.REVOKED -> return next == LegacyParcelMappingStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LegacyParcelMappingStatus) {
        if (!canMoveTo(next)) {
            error("legacy parcel mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy parcel mapping"} $code"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): LegacyParcelMapping {
        return LegacyParcelMapping(
            code = code,
            label = label,
            timezone = timezone,
            shiftCode = shiftCode,
            priority = priority,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy parcel mapping in status requested. */
fun newLegacyParcelMapping(code: String, label: String): LegacyParcelMapping {
    return LegacyParcelMapping(
        code = code,
        label = label,
        timezone = "",
        shiftCode = "",
        priority = 0,
        depot = "",
        status = LegacyParcelMappingStatus.REQUESTED,
    )
}
