package com.acme.transport.emissions.idling.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default idling segment is in its life cycle. */
enum class DefaultIdlingSegmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default idling segment as the idling team keeps it. */
data class DefaultIdlingSegment(
    var code: String,
    var title: String,
    var margin: Double,
    var windowDays: Int,
    var dock: String,
    var timezone: String,
    var checksum: String,
    var priority: Int,
    var pallets: Int,
    var status: DefaultIdlingSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultIdlingSegmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultIdlingSegmentStatus): Boolean {
        when (status) {
            DefaultIdlingSegmentStatus.CREATED -> return next == DefaultIdlingSegmentStatus.PUBLISHED
            DefaultIdlingSegmentStatus.VALIDATED -> return next == DefaultIdlingSegmentStatus.RETIRED
            DefaultIdlingSegmentStatus.PUBLISHED -> return next == DefaultIdlingSegmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultIdlingSegmentStatus) {
        if (!canMoveTo(next)) {
            error("default idling segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default idling segment"} $code"

    fun weightedExposure(): Double = round(windowDays.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): DefaultIdlingSegment {
        return DefaultIdlingSegment(
            code = code,
            title = title,
            margin = margin,
            windowDays = windowDays,
            dock = dock,
            timezone = timezone,
            checksum = checksum,
            priority = priority,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default idling segment in status created. */
fun newDefaultIdlingSegment(code: String, title: String): DefaultIdlingSegment {
    return DefaultIdlingSegment(
        code = code,
        title = title,
        margin = 0.0,
        windowDays = 0,
        dock = "",
        timezone = "",
        checksum = "",
        priority = 0,
        pallets = 0,
        status = DefaultIdlingSegmentStatus.CREATED,
    )
}
