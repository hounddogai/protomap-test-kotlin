package com.acme.transport.customs.clearance.clearancethreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient clearance threshold records of customs's clearance work, and the rules, stores, and
// calculations around them.

/** Where a lenient clearance threshold is in its life cycle. */
enum class LenientClearanceThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lenient clearance threshold. Its code is unique within a tenant. */
data class LenientClearanceThreshold(
    var code: String,
    var label: String,
    var routeCode: String,
    var externalRef: String,
    var threshold: Double,
    var dock: String,
    var fuelLiters: Double,
    var windowDays: Int,
    var tags: MutableList<String>,
    var archived: Boolean,
    var status: LenientClearanceThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientClearanceThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientClearanceThresholdStatus): Boolean {
        when (status) {
            LenientClearanceThresholdStatus.IDLE -> return next == LenientClearanceThresholdStatus.DRAINING || next == LenientClearanceThresholdStatus.STOPPED
            LenientClearanceThresholdStatus.WARMING -> return next == LenientClearanceThresholdStatus.DRAINING || next == LenientClearanceThresholdStatus.STOPPED
            LenientClearanceThresholdStatus.READY -> {
                when (next) {
                    LenientClearanceThresholdStatus.DRAINING -> return true
                }
                return false
            }
            LenientClearanceThresholdStatus.DRAINING -> {
                when (next) {
                    LenientClearanceThresholdStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientClearanceThresholdStatus) {
        if (!canMoveTo(next)) {
            error("lenient clearance threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lenient clearance threshold"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round((threshold - fuelLiters) * 100) / 100

    fun snapshot(): LenientClearanceThreshold {
        return LenientClearanceThreshold(
            code = code,
            label = label,
            routeCode = routeCode,
            externalRef = externalRef,
            threshold = threshold,
            dock = dock,
            fuelLiters = fuelLiters,
            windowDays = windowDays,
            tags = tags,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient clearance threshold in status idle. */
fun newLenientClearanceThreshold(code: String, label: String): LenientClearanceThreshold {
    return LenientClearanceThreshold(
        code = code,
        label = label,
        routeCode = "",
        externalRef = "",
        threshold = 0.0,
        dock = "",
        fuelLiters = 0.0,
        windowDays = 0,
        tags = mutableListOf<String>(),
        archived = false,
        status = LenientClearanceThresholdStatus.IDLE,
    )
}
