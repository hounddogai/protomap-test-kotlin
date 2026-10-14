package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback qualification snapshot is in its life cycle. */
enum class FallbackQualificationSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a fallback qualification snapshot and its current status. */
data class FallbackQualificationSnapshot(
    var code: String,
    var title: String,
    var capacity: Int,
    var locale: String,
    var archived: Boolean,
    var rate: Double,
    var driverId: String,
    var createdAt: Int,
    var startsAt: Int,
    var loadKg: Double,
    var windowDays: Int,
    var status: FallbackQualificationSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackQualificationSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackQualificationSnapshotStatus): Boolean {
        when (status) {
            FallbackQualificationSnapshotStatus.IDLE -> return next == FallbackQualificationSnapshotStatus.READY
            FallbackQualificationSnapshotStatus.WARMING -> return next == FallbackQualificationSnapshotStatus.STOPPED
            FallbackQualificationSnapshotStatus.READY -> {
                when (next) {
                    FallbackQualificationSnapshotStatus.DRAINING, FallbackQualificationSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            FallbackQualificationSnapshotStatus.DRAINING -> return next == FallbackQualificationSnapshotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackQualificationSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("fallback qualification snapshot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedLoad(): Double = round(startsAt.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): FallbackQualificationSnapshot {
        return FallbackQualificationSnapshot(
            code = code,
            title = title,
            capacity = capacity,
            locale = locale,
            archived = archived,
            rate = rate,
            driverId = driverId,
            createdAt = createdAt,
            startsAt = startsAt,
            loadKg = loadKg,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback qualification snapshot in status idle. */
fun newFallbackQualificationSnapshot(code: String, title: String): FallbackQualificationSnapshot {
    return FallbackQualificationSnapshot(
        code = code,
        title = title,
        capacity = 0,
        locale = "",
        archived = false,
        rate = 0.0,
        driverId = "",
        createdAt = 0,
        startsAt = 0,
        loadKg = 0.0,
        windowDays = 0,
        status = FallbackQualificationSnapshotStatus.IDLE,
    )
}
