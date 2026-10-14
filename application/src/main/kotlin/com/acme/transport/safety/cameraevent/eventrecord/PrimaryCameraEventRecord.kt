package com.acme.transport.safety.cameraevent.eventrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary camera event record records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a primary camera event record is in its life cycle. */
enum class PrimaryCameraEventRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a primary camera event record and its current status. */
data class PrimaryCameraEventRecord(
    var code: String,
    var title: String,
    var capacity: Int,
    var channel: String,
    var externalRef: String,
    var tags: MutableList<String>,
    var odometer: Int,
    var status: PrimaryCameraEventRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCameraEventRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCameraEventRecordStatus): Boolean {
        when (status) {
            PrimaryCameraEventRecordStatus.PROPOSED -> {
                when (next) {
                    PrimaryCameraEventRecordStatus.APPROVED -> return true
                }
                return false
            }
            PrimaryCameraEventRecordStatus.APPROVED -> return next == PrimaryCameraEventRecordStatus.WITHDRAWN
            PrimaryCameraEventRecordStatus.REJECTED -> return next == PrimaryCameraEventRecordStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryCameraEventRecordStatus) {
        if (!canMoveTo(next)) {
            error("primary camera event record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedValue(): Double = round(capacity.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): PrimaryCameraEventRecord {
        return PrimaryCameraEventRecord(
            code = code,
            title = title,
            capacity = capacity,
            channel = channel,
            externalRef = externalRef,
            tags = tags,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a primary camera event record in status proposed. */
fun newPrimaryCameraEventRecord(code: String, title: String): PrimaryCameraEventRecord {
    return PrimaryCameraEventRecord(
        code = code,
        title = title,
        capacity = 0,
        channel = "",
        externalRef = "",
        tags = mutableListOf<String>(),
        odometer = 0,
        status = PrimaryCameraEventRecordStatus.PROPOSED,
    )
}
