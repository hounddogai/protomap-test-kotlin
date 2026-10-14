package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted audit segment is in its life cycle. */
enum class WeightedAuditSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One weighted audit segment, with what the planners need to act on it. */
data class WeightedAuditSegment(
    var code: String,
    var title: String,
    var vehicleId: String,
    var name: String,
    var capacity: Int,
    var createdAt: Int,
    var tags: MutableList<String>,
    var visible: Boolean,
    var revision: Int,
    var timezone: String,
    var status: WeightedAuditSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAuditSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAuditSegmentStatus): Boolean {
        when (status) {
            WeightedAuditSegmentStatus.IDLE -> {
                when (next) {
                    WeightedAuditSegmentStatus.WARMING, WeightedAuditSegmentStatus.READY -> return true
                }
                return false
            }
            WeightedAuditSegmentStatus.WARMING -> {
                when (next) {
                    WeightedAuditSegmentStatus.READY -> return true
                }
                return false
            }
            WeightedAuditSegmentStatus.READY -> return next == WeightedAuditSegmentStatus.STOPPED
            WeightedAuditSegmentStatus.DRAINING -> return next == WeightedAuditSegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: WeightedAuditSegmentStatus) {
        if (!canMoveTo(next)) {
            error("weighted audit segment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
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

    fun effectiveValue(): Double = round((createdAt.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): WeightedAuditSegment {
        return WeightedAuditSegment(
            code = code,
            title = title,
            vehicleId = vehicleId,
            name = name,
            capacity = capacity,
            createdAt = createdAt,
            tags = tags,
            visible = visible,
            revision = revision,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted audit segment in status idle. */
fun newWeightedAuditSegment(code: String, title: String): WeightedAuditSegment {
    return WeightedAuditSegment(
        code = code,
        title = title,
        vehicleId = "",
        name = "",
        capacity = 0,
        createdAt = 0,
        tags = mutableListOf<String>(),
        visible = false,
        revision = 0,
        timezone = "",
        status = WeightedAuditSegmentStatus.IDLE,
    )
}
