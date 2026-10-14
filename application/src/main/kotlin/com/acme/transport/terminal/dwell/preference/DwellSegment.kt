package com.acme.transport.terminal.dwell.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell segment is in its life cycle. */
enum class DwellSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A dwell segment. Its code is unique within a tenant. */
data class DwellSegment(
    var code: String,
    var label: String,
    var ratio: Double,
    var revision: Int,
    var ownerId: String,
    var createdAt: Int,
    var cost: Double,
    var expiresAt: Int,
    var budget: Double,
    var status: DwellSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellSegmentStatus): Boolean {
        when (status) {
            DwellSegmentStatus.IDLE -> return next == DwellSegmentStatus.WARMING
            DwellSegmentStatus.WARMING -> {
                when (next) {
                    DwellSegmentStatus.STOPPED -> return true
                }
                return false
            }
            DwellSegmentStatus.READY -> {
                when (next) {
                    DwellSegmentStatus.DRAINING, DwellSegmentStatus.STOPPED -> return true
                }
                return false
            }
            DwellSegmentStatus.DRAINING -> {
                when (next) {
                    DwellSegmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellSegmentStatus) {
        if (!canMoveTo(next)) {
            error("dwell segment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedLoad(): Double = round((budget - cost) * 100) / 100

    fun snapshot(): DwellSegment {
        return DwellSegment(
            code = code,
            label = label,
            ratio = ratio,
            revision = revision,
            ownerId = ownerId,
            createdAt = createdAt,
            cost = cost,
            expiresAt = expiresAt,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dwell segment in status idle. */
fun newDwellSegment(code: String, label: String): DwellSegment {
    return DwellSegment(
        code = code,
        label = label,
        ratio = 0.0,
        revision = 0,
        ownerId = "",
        createdAt = 0,
        cost = 0.0,
        expiresAt = 0,
        budget = 0.0,
        status = DwellSegmentStatus.IDLE,
    )
}
