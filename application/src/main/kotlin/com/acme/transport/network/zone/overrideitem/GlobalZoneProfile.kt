package com.acme.transport.network.zone.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global zone profile is in its life cycle. */
enum class GlobalZoneProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One global zone profile, with what the planners need to act on it. */
data class GlobalZoneProfile(
    var code: String,
    var title: String,
    var rate: Double,
    var segment: String,
    var carrier: String,
    var capacity: Int,
    var threshold: Double,
    var maximum: Double,
    var discount: Double,
    var version: Int,
    var active: Boolean,
    var status: GlobalZoneProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalZoneProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalZoneProfileStatus): Boolean {
        when (status) {
            GlobalZoneProfileStatus.PLANNED -> return next == GlobalZoneProfileStatus.SCHEDULED || next == GlobalZoneProfileStatus.IN_PROGRESS
            GlobalZoneProfileStatus.SCHEDULED -> {
                when (next) {
                    GlobalZoneProfileStatus.IN_PROGRESS, GlobalZoneProfileStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalZoneProfileStatus.IN_PROGRESS -> {
                when (next) {
                    GlobalZoneProfileStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalZoneProfileStatus.COMPLETED -> return next == GlobalZoneProfileStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: GlobalZoneProfileStatus) {
        if (!canMoveTo(next)) {
            error("global zone profile $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun weightedValue(): Double = round(version.toDouble() * maximum * 100) / 100

    fun snapshot(): GlobalZoneProfile {
        return GlobalZoneProfile(
            code = code,
            title = title,
            rate = rate,
            segment = segment,
            carrier = carrier,
            capacity = capacity,
            threshold = threshold,
            maximum = maximum,
            discount = discount,
            version = version,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a global zone profile in status planned. */
fun newGlobalZoneProfile(code: String, title: String): GlobalZoneProfile {
    return GlobalZoneProfile(
        code = code,
        title = title,
        rate = 0.0,
        segment = "",
        carrier = "",
        capacity = 0,
        threshold = 0.0,
        maximum = 0.0,
        discount = 0.0,
        version = 0,
        active = false,
        status = GlobalZoneProfileStatus.PLANNED,
    )
}
