package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard bundle is in its life cycle. */
enum class TrailerYardBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a trailer yard bundle and its current status. */
data class TrailerYardBundle(
    var code: String,
    var name: String,
    var stopCount: Int,
    var trailerId: String,
    var shiftCode: String,
    var timezone: String,
    var margin: Double,
    var discount: Double,
    var label: String,
    var retries: Int,
    var source: String,
    var status: TrailerYardBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardBundleStatus): Boolean {
        when (status) {
            TrailerYardBundleStatus.IDLE -> return next == TrailerYardBundleStatus.READY || next == TrailerYardBundleStatus.DRAINING
            TrailerYardBundleStatus.WARMING -> return next == TrailerYardBundleStatus.READY || next == TrailerYardBundleStatus.DRAINING
            TrailerYardBundleStatus.READY -> return next == TrailerYardBundleStatus.DRAINING
            TrailerYardBundleStatus.DRAINING -> return next == TrailerYardBundleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TrailerYardBundleStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"trailer yard bundle"} $code"

    fun projectedLoad(): Double = round((discount - retries.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardBundle {
        return TrailerYardBundle(
            code = code,
            name = name,
            stopCount = stopCount,
            trailerId = trailerId,
            shiftCode = shiftCode,
            timezone = timezone,
            margin = margin,
            discount = discount,
            label = label,
            retries = retries,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a trailer yard bundle in status idle. */
fun newTrailerYardBundle(code: String, name: String): TrailerYardBundle {
    return TrailerYardBundle(
        code = code,
        name = name,
        stopCount = 0,
        trailerId = "",
        shiftCode = "",
        timezone = "",
        margin = 0.0,
        discount = 0.0,
        label = "",
        retries = 0,
        source = "",
        status = TrailerYardBundleStatus.IDLE,
    )
}
