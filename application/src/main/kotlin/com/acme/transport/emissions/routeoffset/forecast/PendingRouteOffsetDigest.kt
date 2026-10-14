package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.text.normalize

/** Where a pending route offset digest is in its life cycle. */
enum class PendingRouteOffsetDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A pending route offset digest. Its code is unique within a tenant. */
data class PendingRouteOffsetDigest(
    var code: String,
    var title: String,
    var currency: String,
    var vehicleId: String,
    var attempts: Int,
    var status: PendingRouteOffsetDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRouteOffsetDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRouteOffsetDigestStatus): Boolean {
        when (status) {
            PendingRouteOffsetDigestStatus.NEW -> return next == PendingRouteOffsetDigestStatus.DONE
            PendingRouteOffsetDigestStatus.QUEUED -> return next == PendingRouteOffsetDigestStatus.RUNNING
            PendingRouteOffsetDigestStatus.RUNNING -> return next == PendingRouteOffsetDigestStatus.DONE
            PendingRouteOffsetDigestStatus.DONE -> return next == PendingRouteOffsetDigestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PendingRouteOffsetDigestStatus) {
        if (!canMoveTo(next)) {
            error("pending route offset digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): PendingRouteOffsetDigest {
        return PendingRouteOffsetDigest(
            code = code,
            title = title,
            currency = currency,
            vehicleId = vehicleId,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending route offset digest in status new. */
fun newPendingRouteOffsetDigest(code: String, title: String): PendingRouteOffsetDigest {
    return PendingRouteOffsetDigest(
        code = code,
        title = title,
        currency = "",
        vehicleId = "",
        attempts = 0,
        status = PendingRouteOffsetDigestStatus.NEW,
    )
}
