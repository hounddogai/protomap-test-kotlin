package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery segment records of lastmile's redelivery work, and the rules, stores, and calculations
// around them.

/** Where a redelivery segment is in its life cycle. */
enum class RedeliverySegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A redelivery segment. Its code is unique within a tenant. */
data class RedeliverySegment(
    var code: String,
    var title: String,
    var capacity: Int,
    var label: String,
    var driverId: String,
    var endsAt: Int,
    var margin: Double,
    var unit: String,
    var batchSize: Int,
    var status: RedeliverySegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliverySegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliverySegmentStatus): Boolean {
        when (status) {
            RedeliverySegmentStatus.REQUESTED -> {
                when (next) {
                    RedeliverySegmentStatus.GRANTED, RedeliverySegmentStatus.REVOKED -> return true
                }
                return false
            }
            RedeliverySegmentStatus.GRANTED -> return next == RedeliverySegmentStatus.REVOKED
            RedeliverySegmentStatus.REVOKED -> {
                when (next) {
                    RedeliverySegmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliverySegmentStatus) {
        if (!canMoveTo(next)) {
            error("redelivery segment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"redelivery segment"} $code"

    fun adjustedValue(): Double = round(batchSize.toDouble() * margin * 100) / 100

    fun snapshot(): RedeliverySegment {
        return RedeliverySegment(
            code = code,
            title = title,
            capacity = capacity,
            label = label,
            driverId = driverId,
            endsAt = endsAt,
            margin = margin,
            unit = unit,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a redelivery segment in status requested. */
fun newRedeliverySegment(code: String, title: String): RedeliverySegment {
    return RedeliverySegment(
        code = code,
        title = title,
        capacity = 0,
        label = "",
        driverId = "",
        endsAt = 0,
        margin = 0.0,
        unit = "",
        batchSize = 0,
        status = RedeliverySegmentStatus.REQUESTED,
    )
}
