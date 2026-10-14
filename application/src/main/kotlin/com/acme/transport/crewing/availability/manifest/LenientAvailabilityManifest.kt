package com.acme.transport.crewing.availability.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient availability manifest records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a lenient availability manifest is in its life cycle. */
enum class LenientAvailabilityManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One lenient availability manifest, with what the planners need to act on it. */
data class LenientAvailabilityManifest(
    var code: String,
    var title: String,
    var externalRef: String,
    var windowDays: Int,
    var endsAt: Int,
    var routeCode: String,
    var maximum: Double,
    var status: LenientAvailabilityManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAvailabilityManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAvailabilityManifestStatus): Boolean {
        when (status) {
            LenientAvailabilityManifestStatus.IDLE -> {
                when (next) {
                    LenientAvailabilityManifestStatus.READY -> return true
                }
                return false
            }
            LenientAvailabilityManifestStatus.WARMING -> return next == LenientAvailabilityManifestStatus.READY || next == LenientAvailabilityManifestStatus.STOPPED
            LenientAvailabilityManifestStatus.READY -> {
                when (next) {
                    LenientAvailabilityManifestStatus.DRAINING, LenientAvailabilityManifestStatus.STOPPED -> return true
                }
                return false
            }
            LenientAvailabilityManifestStatus.DRAINING -> {
                when (next) {
                    LenientAvailabilityManifestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientAvailabilityManifestStatus) {
        if (!canMoveTo(next)) {
            error("lenient availability manifest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveTotal(): Double = round((windowDays.toDouble() - maximum) * 100) / 100

    fun snapshot(): LenientAvailabilityManifest {
        return LenientAvailabilityManifest(
            code = code,
            title = title,
            externalRef = externalRef,
            windowDays = windowDays,
            endsAt = endsAt,
            routeCode = routeCode,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient availability manifest in status idle. */
fun newLenientAvailabilityManifest(code: String, title: String): LenientAvailabilityManifest {
    return LenientAvailabilityManifest(
        code = code,
        title = title,
        externalRef = "",
        windowDays = 0,
        endsAt = 0,
        routeCode = "",
        maximum = 0.0,
        status = LenientAvailabilityManifestStatus.IDLE,
    )
}
