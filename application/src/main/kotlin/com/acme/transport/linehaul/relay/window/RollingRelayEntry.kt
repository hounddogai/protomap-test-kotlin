package com.acme.transport.linehaul.relay.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling relay entry is in its life cycle. */
enum class RollingRelayEntryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rolling relay entry as the relay team keeps it. */
data class RollingRelayEntry(
    var code: String,
    var name: String,
    var reviewer: String,
    var carrier: String,
    var source: String,
    var region: String,
    var attempts: Int,
    var ceiling: Double,
    var lane: String,
    var externalRef: String,
    var etaMinutes: Int,
    var status: RollingRelayEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRelayEntryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRelayEntryStatus): Boolean {
        when (status) {
            RollingRelayEntryStatus.PLANNED -> return next == RollingRelayEntryStatus.IN_PROGRESS || next == RollingRelayEntryStatus.COMPLETED
            RollingRelayEntryStatus.SCHEDULED -> {
                when (next) {
                    RollingRelayEntryStatus.CANCELLED -> return true
                }
                return false
            }
            RollingRelayEntryStatus.IN_PROGRESS -> return next == RollingRelayEntryStatus.COMPLETED
            RollingRelayEntryStatus.COMPLETED -> {
                when (next) {
                    RollingRelayEntryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingRelayEntryStatus) {
        if (!canMoveTo(next)) {
            error("rolling relay entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveVolume(): Double = round(attempts.toDouble() * ceiling * 100) / 100

    fun snapshot(): RollingRelayEntry {
        return RollingRelayEntry(
            code = code,
            name = name,
            reviewer = reviewer,
            carrier = carrier,
            source = source,
            region = region,
            attempts = attempts,
            ceiling = ceiling,
            lane = lane,
            externalRef = externalRef,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rolling relay entry in status planned. */
fun newRollingRelayEntry(code: String, name: String): RollingRelayEntry {
    return RollingRelayEntry(
        code = code,
        name = name,
        reviewer = "",
        carrier = "",
        source = "",
        region = "",
        attempts = 0,
        ceiling = 0.0,
        lane = "",
        externalRef = "",
        etaMinutes = 0,
        status = RollingRelayEntryStatus.PLANNED,
    )
}
