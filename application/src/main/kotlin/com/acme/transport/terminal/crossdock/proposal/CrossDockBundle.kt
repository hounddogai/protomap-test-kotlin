package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock bundle is in its life cycle. */
enum class CrossDockBundleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One cross dock bundle, with what the planners need to act on it. */
data class CrossDockBundle(
    var code: String,
    var title: String,
    var lane: String,
    var floorValue: Double,
    var region: String,
    var ratio: Double,
    var maximum: Double,
    var segment: String,
    var externalRef: String,
    var sequence: Int,
    var status: CrossDockBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockBundleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockBundleStatus): Boolean {
        when (status) {
            CrossDockBundleStatus.PROPOSED -> {
                when (next) {
                    CrossDockBundleStatus.APPROVED -> return true
                }
                return false
            }
            CrossDockBundleStatus.APPROVED -> return next == CrossDockBundleStatus.REJECTED || next == CrossDockBundleStatus.WITHDRAWN
            CrossDockBundleStatus.REJECTED -> {
                when (next) {
                    CrossDockBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockBundleStatus) {
        if (!canMoveTo(next)) {
            error("cross dock bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedExposure(): Double = round((floorValue - ratio) * 100) / 100

    fun snapshot(): CrossDockBundle {
        return CrossDockBundle(
            code = code,
            title = title,
            lane = lane,
            floorValue = floorValue,
            region = region,
            ratio = ratio,
            maximum = maximum,
            segment = segment,
            externalRef = externalRef,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a cross dock bundle in status proposed. */
fun newCrossDockBundle(code: String, title: String): CrossDockBundle {
    return CrossDockBundle(
        code = code,
        title = title,
        lane = "",
        floorValue = 0.0,
        region = "",
        ratio = 0.0,
        maximum = 0.0,
        segment = "",
        externalRef = "",
        sequence = 0,
        status = CrossDockBundleStatus.PROPOSED,
    )
}
