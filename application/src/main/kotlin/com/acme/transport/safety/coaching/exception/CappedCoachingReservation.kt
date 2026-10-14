package com.acme.transport.safety.coaching.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped coaching reservation is in its life cycle. */
enum class CappedCoachingReservationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped coaching reservation as the coaching team keeps it. */
data class CappedCoachingReservation(
    var code: String,
    var title: String,
    var ratio: Double,
    var stopCount: Int,
    var category: String,
    var attributes: MutableMap<String, String>,
    var tags: MutableList<String>,
    var shiftCode: String,
    var endsAt: Int,
    var status: CappedCoachingReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCoachingReservationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCoachingReservationStatus): Boolean {
        when (status) {
            CappedCoachingReservationStatus.NEW -> return next == CappedCoachingReservationStatus.RUNNING
            CappedCoachingReservationStatus.QUEUED -> return next == CappedCoachingReservationStatus.RUNNING
            CappedCoachingReservationStatus.RUNNING -> return next == CappedCoachingReservationStatus.DONE || next == CappedCoachingReservationStatus.FAILED
            CappedCoachingReservationStatus.DONE -> return next == CappedCoachingReservationStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedCoachingReservationStatus) {
        if (!canMoveTo(next)) {
            error("capped coaching reservation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round((ratio - endsAt.toDouble()) * 100) / 100

    fun snapshot(): CappedCoachingReservation {
        return CappedCoachingReservation(
            code = code,
            title = title,
            ratio = ratio,
            stopCount = stopCount,
            category = category,
            attributes = attributes,
            tags = tags,
            shiftCode = shiftCode,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped coaching reservation in status new. */
fun newCappedCoachingReservation(code: String, title: String): CappedCoachingReservation {
    return CappedCoachingReservation(
        code = code,
        title = title,
        ratio = 0.0,
        stopCount = 0,
        category = "",
        attributes = mutableMapOf<String, String>(),
        tags = mutableListOf<String>(),
        shiftCode = "",
        endsAt = 0,
        status = CappedCoachingReservationStatus.NEW,
    )
}
