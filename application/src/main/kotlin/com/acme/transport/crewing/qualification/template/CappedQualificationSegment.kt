package com.acme.transport.crewing.qualification.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped qualification segment is in its life cycle. */
enum class CappedQualificationSegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One capped qualification segment, with what the planners need to act on it. */
data class CappedQualificationSegment(
    var code: String,
    var title: String,
    var notes: String,
    var lane: String,
    var fuelLiters: Double,
    var floorValue: Double,
    var routeCode: String,
    var visible: Boolean,
    var source: String,
    var status: CappedQualificationSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedQualificationSegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedQualificationSegmentStatus): Boolean {
        when (status) {
            CappedQualificationSegmentStatus.DRAFT -> {
                when (next) {
                    CappedQualificationSegmentStatus.CLOSED -> return true
                }
                return false
            }
            CappedQualificationSegmentStatus.PENDING -> {
                when (next) {
                    CappedQualificationSegmentStatus.SUSPENDED, CappedQualificationSegmentStatus.CLOSED -> return true
                }
                return false
            }
            CappedQualificationSegmentStatus.ACTIVE -> {
                when (next) {
                    CappedQualificationSegmentStatus.CLOSED -> return true
                }
                return false
            }
            CappedQualificationSegmentStatus.SUSPENDED -> return next == CappedQualificationSegmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedQualificationSegmentStatus) {
        if (!canMoveTo(next)) {
            error("capped qualification segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveLoad(): Double = round(fuelLiters * floorValue * 100) / 100

    fun snapshot(): CappedQualificationSegment {
        return CappedQualificationSegment(
            code = code,
            title = title,
            notes = notes,
            lane = lane,
            fuelLiters = fuelLiters,
            floorValue = floorValue,
            routeCode = routeCode,
            visible = visible,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capped qualification segment in status draft. */
fun newCappedQualificationSegment(code: String, title: String): CappedQualificationSegment {
    return CappedQualificationSegment(
        code = code,
        title = title,
        notes = "",
        lane = "",
        fuelLiters = 0.0,
        floorValue = 0.0,
        routeCode = "",
        visible = false,
        source = "",
        status = CappedQualificationSegmentStatus.DRAFT,
    )
}
