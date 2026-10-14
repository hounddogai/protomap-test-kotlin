package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay segment is in its life cycle. */
enum class RelaySegmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A relay segment as the relay team keeps it. */
data class RelaySegment(
    var code: String,
    var label: String,
    var graceDays: Int,
    var driverId: String,
    var margin: Double,
    var segment: String,
    var status: RelaySegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelaySegmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelaySegmentStatus): Boolean {
        when (status) {
            RelaySegmentStatus.CREATED -> return next == RelaySegmentStatus.VALIDATED || next == RelaySegmentStatus.PUBLISHED
            RelaySegmentStatus.VALIDATED -> return next == RelaySegmentStatus.PUBLISHED || next == RelaySegmentStatus.RETIRED
            RelaySegmentStatus.PUBLISHED -> return next == RelaySegmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RelaySegmentStatus) {
        if (!canMoveTo(next)) {
            error("relay segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedVolume(): Double = round(margin * graceDays.toDouble() * 100) / 100

    fun snapshot(): RelaySegment {
        return RelaySegment(
            code = code,
            label = label,
            graceDays = graceDays,
            driverId = driverId,
            margin = margin,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a relay segment in status created. */
fun newRelaySegment(code: String, label: String): RelaySegment {
    return RelaySegment(
        code = code,
        label = label,
        graceDays = 0,
        driverId = "",
        margin = 0.0,
        segment = "",
        status = RelaySegmentStatus.CREATED,
    )
}
