package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling segment is in its life cycle. */
enum class IdlingSegmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A idling segment. Its code is unique within a tenant. */
data class IdlingSegment(
    var code: String,
    var name: String,
    var ceiling: Double,
    var loadKg: Double,
    var graceDays: Int,
    var stopCount: Int,
    var summary: String,
    var description: String,
    var sequence: Int,
    var status: IdlingSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingSegmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingSegmentStatus): Boolean {
        when (status) {
            IdlingSegmentStatus.CREATED -> return next == IdlingSegmentStatus.VALIDATED
            IdlingSegmentStatus.VALIDATED -> {
                when (next) {
                    IdlingSegmentStatus.PUBLISHED -> return true
                }
                return false
            }
            IdlingSegmentStatus.PUBLISHED -> {
                when (next) {
                    IdlingSegmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingSegmentStatus) {
        if (!canMoveTo(next)) {
            error("idling segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedValue(): Double = round((ceiling - loadKg) * 100) / 100

    fun snapshot(): IdlingSegment {
        return IdlingSegment(
            code = code,
            name = name,
            ceiling = ceiling,
            loadKg = loadKg,
            graceDays = graceDays,
            stopCount = stopCount,
            summary = summary,
            description = description,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a idling segment in status created. */
fun newIdlingSegment(code: String, name: String): IdlingSegment {
    return IdlingSegment(
        code = code,
        name = name,
        ceiling = 0.0,
        loadKg = 0.0,
        graceDays = 0,
        stopCount = 0,
        summary = "",
        description = "",
        sequence = 0,
        status = IdlingSegmentStatus.CREATED,
    )
}
