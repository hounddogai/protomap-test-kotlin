package com.acme.transport.lastmile.locker.lockerprofile

import com.acme.transport.kit.text.normalize

/** Where a seasonal locker segment is in its life cycle. */
enum class SeasonalLockerSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A seasonal locker segment as the locker team keeps it. */
data class SeasonalLockerSegment(
    var code: String,
    var title: String,
    var name: String,
    var visible: Boolean,
    var updatedAt: Int,
    var vehicleId: String,
    var notes: String,
    var dock: String,
    var archived: Boolean,
    var checksum: String,
    var approved: Boolean,
    var status: SeasonalLockerSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalLockerSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalLockerSegmentStatus): Boolean {
        when (status) {
            SeasonalLockerSegmentStatus.PLANNED -> {
                when (next) {
                    SeasonalLockerSegmentStatus.IN_PROGRESS, SeasonalLockerSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalLockerSegmentStatus.SCHEDULED -> {
                when (next) {
                    SeasonalLockerSegmentStatus.IN_PROGRESS, SeasonalLockerSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalLockerSegmentStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalLockerSegmentStatus.COMPLETED, SeasonalLockerSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalLockerSegmentStatus.COMPLETED -> return next == SeasonalLockerSegmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalLockerSegmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal locker segment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal locker segment"} $code"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): SeasonalLockerSegment {
        return SeasonalLockerSegment(
            code = code,
            title = title,
            name = name,
            visible = visible,
            updatedAt = updatedAt,
            vehicleId = vehicleId,
            notes = notes,
            dock = dock,
            archived = archived,
            checksum = checksum,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal locker segment in status planned. */
fun newSeasonalLockerSegment(code: String, title: String): SeasonalLockerSegment {
    return SeasonalLockerSegment(
        code = code,
        title = title,
        name = "",
        visible = false,
        updatedAt = 0,
        vehicleId = "",
        notes = "",
        dock = "",
        archived = false,
        checksum = "",
        approved = false,
        status = SeasonalLockerSegmentStatus.PLANNED,
    )
}
