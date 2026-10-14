package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock digest is in its life cycle. */
enum class CrossDockDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a cross dock digest and its current status. */
data class CrossDockDigest(
    var code: String,
    var title: String,
    var rate: Double,
    var floorValue: Double,
    var trailerId: String,
    var dock: String,
    var revision: Int,
    var pinned: Boolean,
    var vehicleId: String,
    var status: CrossDockDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockDigestStatus): Boolean {
        when (status) {
            CrossDockDigestStatus.OPEN -> return next == CrossDockDigestStatus.RESOLVED || next == CrossDockDigestStatus.ARCHIVED
            CrossDockDigestStatus.IN_REVIEW -> return next == CrossDockDigestStatus.RESOLVED || next == CrossDockDigestStatus.ARCHIVED
            CrossDockDigestStatus.RESOLVED -> {
                when (next) {
                    CrossDockDigestStatus.ARCHIVED -> return true
                }
                return false
            }
            CrossDockDigestStatus.REOPENED -> return next == CrossDockDigestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CrossDockDigestStatus) {
        if (!canMoveTo(next)) {
            error("cross dock digest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"cross dock digest"} $code"

    fun weightedLoad(): Double = round((rate - revision.toDouble()) * 100) / 100

    fun snapshot(): CrossDockDigest {
        return CrossDockDigest(
            code = code,
            title = title,
            rate = rate,
            floorValue = floorValue,
            trailerId = trailerId,
            dock = dock,
            revision = revision,
            pinned = pinned,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cross dock digest in status open. */
fun newCrossDockDigest(code: String, title: String): CrossDockDigest {
    return CrossDockDigest(
        code = code,
        title = title,
        rate = 0.0,
        floorValue = 0.0,
        trailerId = "",
        dock = "",
        revision = 0,
        pinned = false,
        vehicleId = "",
        status = CrossDockDigestStatus.OPEN,
    )
}
