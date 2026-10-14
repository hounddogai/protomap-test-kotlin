package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.kit.text.normalize

/** Where a scheduled parcel mapping is in its life cycle. */
enum class ScheduledParcelMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scheduled parcel mapping. Its code is unique within a tenant. */
data class ScheduledParcelMapping(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var notes: String,
    var visible: Boolean,
    var status: ScheduledParcelMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledParcelMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledParcelMappingStatus): Boolean {
        when (status) {
            ScheduledParcelMappingStatus.REQUESTED -> {
                when (next) {
                    ScheduledParcelMappingStatus.GRANTED -> return true
                }
                return false
            }
            ScheduledParcelMappingStatus.GRANTED -> return next == ScheduledParcelMappingStatus.EXPIRED
            ScheduledParcelMappingStatus.REVOKED -> return next == ScheduledParcelMappingStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScheduledParcelMappingStatus) {
        if (!canMoveTo(next)) {
            error("scheduled parcel mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): ScheduledParcelMapping {
        return ScheduledParcelMapping(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            notes = notes,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled parcel mapping in status requested. */
fun newScheduledParcelMapping(code: String, title: String): ScheduledParcelMapping {
    return ScheduledParcelMapping(
        code = code,
        title = title,
        durationMinutes = 0,
        notes = "",
        visible = false,
        status = ScheduledParcelMappingStatus.REQUESTED,
    )
}
