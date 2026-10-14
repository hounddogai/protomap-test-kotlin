package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped locker assignment is in its life cycle. */
enum class CappedLockerAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capped locker assignment. Its code is unique within a tenant. */
data class CappedLockerAssignment(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var windowDays: Int,
    var lane: String,
    var archived: Boolean,
    var minimum: Double,
    var ratio: Double,
    var batchSize: Int,
    var attributes: MutableMap<String, String>,
    var status: CappedLockerAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedLockerAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedLockerAssignmentStatus): Boolean {
        when (status) {
            CappedLockerAssignmentStatus.CREATED -> return next == CappedLockerAssignmentStatus.RETIRED
            CappedLockerAssignmentStatus.VALIDATED -> return next == CappedLockerAssignmentStatus.PUBLISHED
            CappedLockerAssignmentStatus.PUBLISHED -> {
                when (next) {
                    CappedLockerAssignmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedLockerAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("capped locker assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round(windowDays.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): CappedLockerAssignment {
        return CappedLockerAssignment(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            windowDays = windowDays,
            lane = lane,
            archived = archived,
            minimum = minimum,
            ratio = ratio,
            batchSize = batchSize,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capped locker assignment in status created. */
fun newCappedLockerAssignment(code: String, title: String): CappedLockerAssignment {
    return CappedLockerAssignment(
        code = code,
        title = title,
        durationMinutes = 0,
        windowDays = 0,
        lane = "",
        archived = false,
        minimum = 0.0,
        ratio = 0.0,
        batchSize = 0,
        attributes = mutableMapOf<String, String>(),
        status = CappedLockerAssignmentStatus.CREATED,
    )
}
