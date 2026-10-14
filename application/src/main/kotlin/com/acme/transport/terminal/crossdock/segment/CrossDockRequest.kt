package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock request is in its life cycle. */
enum class CrossDockRequestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A cross dock request as the cross dock team keeps it. */
data class CrossDockRequest(
    var code: String,
    var label: String,
    var revision: Int,
    var dock: String,
    var ceiling: Double,
    var cost: Double,
    var ratio: Double,
    var status: CrossDockRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockRequestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockRequestStatus): Boolean {
        when (status) {
            CrossDockRequestStatus.IDLE -> {
                when (next) {
                    CrossDockRequestStatus.WARMING, CrossDockRequestStatus.STOPPED -> return true
                }
                return false
            }
            CrossDockRequestStatus.WARMING -> {
                when (next) {
                    CrossDockRequestStatus.READY -> return true
                }
                return false
            }
            CrossDockRequestStatus.READY -> return next == CrossDockRequestStatus.DRAINING
            CrossDockRequestStatus.DRAINING -> return next == CrossDockRequestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CrossDockRequestStatus) {
        if (!canMoveTo(next)) {
            error("cross dock request $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedVolume(): Double = round((cost - revision.toDouble()) * 100) / 100

    fun snapshot(): CrossDockRequest {
        return CrossDockRequest(
            code = code,
            label = label,
            revision = revision,
            dock = dock,
            ceiling = ceiling,
            cost = cost,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a cross dock request in status idle. */
fun newCrossDockRequest(code: String, label: String): CrossDockRequest {
    return CrossDockRequest(
        code = code,
        label = label,
        revision = 0,
        dock = "",
        ceiling = 0.0,
        cost = 0.0,
        ratio = 0.0,
        status = CrossDockRequestStatus.IDLE,
    )
}
