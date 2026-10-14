package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default transit time manifest records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a default transit time manifest is in its life cycle. */
enum class DefaultTransitTimeManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One default transit time manifest, with what the planners need to act on it. */
data class DefaultTransitTimeManifest(
    var code: String,
    var title: String,
    var stopCount: Int,
    var threshold: Double,
    var timezone: String,
    var amount: Double,
    var priority: Int,
    var version: Int,
    var pinned: Boolean,
    var vehicleId: String,
    var status: DefaultTransitTimeManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTransitTimeManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTransitTimeManifestStatus): Boolean {
        when (status) {
            DefaultTransitTimeManifestStatus.NEW -> {
                when (next) {
                    DefaultTransitTimeManifestStatus.RUNNING, DefaultTransitTimeManifestStatus.DONE -> return true
                }
                return false
            }
            DefaultTransitTimeManifestStatus.QUEUED -> return next == DefaultTransitTimeManifestStatus.FAILED
            DefaultTransitTimeManifestStatus.RUNNING -> return next == DefaultTransitTimeManifestStatus.DONE || next == DefaultTransitTimeManifestStatus.FAILED
            DefaultTransitTimeManifestStatus.DONE -> return next == DefaultTransitTimeManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultTransitTimeManifestStatus) {
        if (!canMoveTo(next)) {
            error("default transit time manifest $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"default transit time manifest"} $code"

    fun projectedVolume(): Double = round((priority.toDouble() + threshold) * 100) / 100

    fun snapshot(): DefaultTransitTimeManifest {
        return DefaultTransitTimeManifest(
            code = code,
            title = title,
            stopCount = stopCount,
            threshold = threshold,
            timezone = timezone,
            amount = amount,
            priority = priority,
            version = version,
            pinned = pinned,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default transit time manifest in status new. */
fun newDefaultTransitTimeManifest(code: String, title: String): DefaultTransitTimeManifest {
    return DefaultTransitTimeManifest(
        code = code,
        title = title,
        stopCount = 0,
        threshold = 0.0,
        timezone = "",
        amount = 0.0,
        priority = 0,
        version = 0,
        pinned = false,
        vehicleId = "",
        status = DefaultTransitTimeManifestStatus.NEW,
    )
}
