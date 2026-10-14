package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global service area segment is in its life cycle. */
enum class GlobalServiceAreaSegmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One global service area segment, with what the planners need to act on it. */
data class GlobalServiceAreaSegment(
    var code: String,
    var label: String,
    var notes: String,
    var depot: String,
    var checksum: String,
    var etaMinutes: Int,
    var driverId: String,
    var ceiling: Double,
    var source: String,
    var status: GlobalServiceAreaSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalServiceAreaSegmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalServiceAreaSegmentStatus): Boolean {
        when (status) {
            GlobalServiceAreaSegmentStatus.CREATED -> return next == GlobalServiceAreaSegmentStatus.VALIDATED || next == GlobalServiceAreaSegmentStatus.PUBLISHED
            GlobalServiceAreaSegmentStatus.VALIDATED -> return next == GlobalServiceAreaSegmentStatus.PUBLISHED || next == GlobalServiceAreaSegmentStatus.RETIRED
            GlobalServiceAreaSegmentStatus.PUBLISHED -> {
                when (next) {
                    GlobalServiceAreaSegmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalServiceAreaSegmentStatus) {
        if (!canMoveTo(next)) {
            error("global service area segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global service area segment"} $code"

    fun weightedExposure(): Double = round(etaMinutes.toDouble() * ceiling * 100) / 100

    fun snapshot(): GlobalServiceAreaSegment {
        return GlobalServiceAreaSegment(
            code = code,
            label = label,
            notes = notes,
            depot = depot,
            checksum = checksum,
            etaMinutes = etaMinutes,
            driverId = driverId,
            ceiling = ceiling,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global service area segment in status created. */
fun newGlobalServiceAreaSegment(code: String, label: String): GlobalServiceAreaSegment {
    return GlobalServiceAreaSegment(
        code = code,
        label = label,
        notes = "",
        depot = "",
        checksum = "",
        etaMinutes = 0,
        driverId = "",
        ceiling = 0.0,
        source = "",
        status = GlobalServiceAreaSegmentStatus.CREATED,
    )
}
