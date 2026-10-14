package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.kit.text.normalize

// The parcel schedule records of lastmile's parcel work, and the rules, stores, and calculations around them.

/** Where a parcel schedule is in its life cycle. */
enum class ParcelScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A parcel schedule. Its code is unique within a tenant. */
data class ParcelSchedule(
    var code: String,
    var title: String,
    var dock: String,
    var checksum: String,
    var externalRef: String,
    var floorValue: Double,
    var status: ParcelScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelScheduleStatus): Boolean {
        when (status) {
            ParcelScheduleStatus.DRAFT -> {
                when (next) {
                    ParcelScheduleStatus.ACTIVE, ParcelScheduleStatus.SUSPENDED -> return true
                }
                return false
            }
            ParcelScheduleStatus.PENDING -> {
                when (next) {
                    ParcelScheduleStatus.ACTIVE -> return true
                }
                return false
            }
            ParcelScheduleStatus.ACTIVE -> return next == ParcelScheduleStatus.SUSPENDED
            ParcelScheduleStatus.SUSPENDED -> {
                when (next) {
                    ParcelScheduleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelScheduleStatus) {
        if (!canMoveTo(next)) {
            error("parcel schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = floorValue

    fun snapshot(): ParcelSchedule {
        return ParcelSchedule(
            code = code,
            title = title,
            dock = dock,
            checksum = checksum,
            externalRef = externalRef,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a parcel schedule in status draft. */
fun newParcelSchedule(code: String, title: String): ParcelSchedule {
    return ParcelSchedule(
        code = code,
        title = title,
        dock = "",
        checksum = "",
        externalRef = "",
        floorValue = 0.0,
        status = ParcelScheduleStatus.DRAFT,
    )
}
