package com.acme.transport.customs.broker.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled broker digest is in its life cycle. */
enum class ScheduledBrokerDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One scheduled broker digest, with what the planners need to act on it. */
data class ScheduledBrokerDigest(
    var code: String,
    var label: String,
    var visible: Boolean,
    var weight: Double,
    var capacity: Int,
    var reviewer: String,
    var quantity: Int,
    var status: ScheduledBrokerDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledBrokerDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledBrokerDigestStatus): Boolean {
        when (status) {
            ScheduledBrokerDigestStatus.OPEN -> return next == ScheduledBrokerDigestStatus.IN_REVIEW || next == ScheduledBrokerDigestStatus.REOPENED
            ScheduledBrokerDigestStatus.IN_REVIEW -> return next == ScheduledBrokerDigestStatus.RESOLVED
            ScheduledBrokerDigestStatus.RESOLVED -> {
                when (next) {
                    ScheduledBrokerDigestStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledBrokerDigestStatus.REOPENED -> return next == ScheduledBrokerDigestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledBrokerDigestStatus) {
        if (!canMoveTo(next)) {
            error("scheduled broker digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedVolume(): Double = round((weight + capacity.toDouble()) * 100) / 100

    fun snapshot(): ScheduledBrokerDigest {
        return ScheduledBrokerDigest(
            code = code,
            label = label,
            visible = visible,
            weight = weight,
            capacity = capacity,
            reviewer = reviewer,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled broker digest in status open. */
fun newScheduledBrokerDigest(code: String, label: String): ScheduledBrokerDigest {
    return ScheduledBrokerDigest(
        code = code,
        label = label,
        visible = false,
        weight = 0.0,
        capacity = 0,
        reviewer = "",
        quantity = 0,
        status = ScheduledBrokerDigestStatus.OPEN,
    )
}
