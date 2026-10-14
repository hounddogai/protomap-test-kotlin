package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.text.normalize

/** Where a pending utilization digest is in its life cycle. */
enum class PendingUtilizationDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One pending utilization digest, with what the planners need to act on it. */
data class PendingUtilizationDigest(
    var code: String,
    var label: String,
    var archived: Boolean,
    var title: String,
    var timezone: String,
    var loadKg: Double,
    var driverId: String,
    var routeCode: String,
    var status: PendingUtilizationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingUtilizationDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingUtilizationDigestStatus): Boolean {
        when (status) {
            PendingUtilizationDigestStatus.IDLE -> return next == PendingUtilizationDigestStatus.DRAINING
            PendingUtilizationDigestStatus.WARMING -> return next == PendingUtilizationDigestStatus.DRAINING
            PendingUtilizationDigestStatus.READY -> return next == PendingUtilizationDigestStatus.DRAINING || next == PendingUtilizationDigestStatus.STOPPED
            PendingUtilizationDigestStatus.DRAINING -> return next == PendingUtilizationDigestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PendingUtilizationDigestStatus) {
        if (!canMoveTo(next)) {
            error("pending utilization digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = loadKg

    fun snapshot(): PendingUtilizationDigest {
        return PendingUtilizationDigest(
            code = code,
            label = label,
            archived = archived,
            title = title,
            timezone = timezone,
            loadKg = loadKg,
            driverId = driverId,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending utilization digest in status idle. */
fun newPendingUtilizationDigest(code: String, label: String): PendingUtilizationDigest {
    return PendingUtilizationDigest(
        code = code,
        label = label,
        archived = false,
        title = "",
        timezone = "",
        loadKg = 0.0,
        driverId = "",
        routeCode = "",
        status = PendingUtilizationDigestStatus.IDLE,
    )
}
