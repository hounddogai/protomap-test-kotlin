package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification segment is in its life cycle. */
enum class QualificationSegmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A qualification segment as the qualification team keeps it. */
data class QualificationSegment(
    var code: String,
    var name: String,
    var version: Int,
    var lane: String,
    var routeCode: String,
    var region: String,
    var pinned: Boolean,
    var title: String,
    var pallets: Int,
    var maximum: Double,
    var loadKg: Double,
    var status: QualificationSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationSegmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationSegmentStatus): Boolean {
        when (status) {
            QualificationSegmentStatus.CREATED -> return next == QualificationSegmentStatus.PUBLISHED || next == QualificationSegmentStatus.RETIRED
            QualificationSegmentStatus.VALIDATED -> {
                when (next) {
                    QualificationSegmentStatus.PUBLISHED -> return true
                }
                return false
            }
            QualificationSegmentStatus.PUBLISHED -> {
                when (next) {
                    QualificationSegmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationSegmentStatus) {
        if (!canMoveTo(next)) {
            error("qualification segment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun weightedVolume(): Double = round((version.toDouble() + loadKg) * 100) / 100

    fun snapshot(): QualificationSegment {
        return QualificationSegment(
            code = code,
            name = name,
            version = version,
            lane = lane,
            routeCode = routeCode,
            region = region,
            pinned = pinned,
            title = title,
            pallets = pallets,
            maximum = maximum,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a qualification segment in status created. */
fun newQualificationSegment(code: String, name: String): QualificationSegment {
    return QualificationSegment(
        code = code,
        name = name,
        version = 0,
        lane = "",
        routeCode = "",
        region = "",
        pinned = false,
        title = "",
        pallets = 0,
        maximum = 0.0,
        loadKg = 0.0,
        status = QualificationSegmentStatus.CREATED,
    )
}
