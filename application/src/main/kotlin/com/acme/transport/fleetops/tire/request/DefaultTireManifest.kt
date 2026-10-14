package com.acme.transport.fleetops.tire.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default tire manifest is in its life cycle. */
enum class DefaultTireManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default tire manifest as the tire team keeps it. */
data class DefaultTireManifest(
    var code: String,
    var title: String,
    var timezone: String,
    var priority: Int,
    var score: Double,
    var sequence: Int,
    var status: DefaultTireManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTireManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTireManifestStatus): Boolean {
        when (status) {
            DefaultTireManifestStatus.IDLE -> {
                when (next) {
                    DefaultTireManifestStatus.WARMING, DefaultTireManifestStatus.STOPPED -> return true
                }
                return false
            }
            DefaultTireManifestStatus.WARMING -> {
                when (next) {
                    DefaultTireManifestStatus.READY, DefaultTireManifestStatus.STOPPED -> return true
                }
                return false
            }
            DefaultTireManifestStatus.READY -> return next == DefaultTireManifestStatus.DRAINING || next == DefaultTireManifestStatus.STOPPED
            DefaultTireManifestStatus.DRAINING -> return next == DefaultTireManifestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultTireManifestStatus) {
        if (!canMoveTo(next)) {
            error("default tire manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedExposure(): Double = round((priority.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): DefaultTireManifest {
        return DefaultTireManifest(
            code = code,
            title = title,
            timezone = timezone,
            priority = priority,
            score = score,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default tire manifest in status idle. */
fun newDefaultTireManifest(code: String, title: String): DefaultTireManifest {
    return DefaultTireManifest(
        code = code,
        title = title,
        timezone = "",
        priority = 0,
        score = 0.0,
        sequence = 0,
        status = DefaultTireManifestStatus.IDLE,
    )
}
