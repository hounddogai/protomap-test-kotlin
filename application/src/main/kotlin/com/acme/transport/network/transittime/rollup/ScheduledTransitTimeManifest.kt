package com.acme.transport.network.transittime.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled transit time manifest is in its life cycle. */
enum class ScheduledTransitTimeManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One scheduled transit time manifest, with what the planners need to act on it. */
data class ScheduledTransitTimeManifest(
    var code: String,
    var name: String,
    var score: Double,
    var capacity: Int,
    var startsAt: Int,
    var status: ScheduledTransitTimeManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTransitTimeManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTransitTimeManifestStatus): Boolean {
        when (status) {
            ScheduledTransitTimeManifestStatus.PLANNED -> {
                when (next) {
                    ScheduledTransitTimeManifestStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledTransitTimeManifestStatus.SCHEDULED -> {
                when (next) {
                    ScheduledTransitTimeManifestStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ScheduledTransitTimeManifestStatus.IN_PROGRESS -> {
                when (next) {
                    ScheduledTransitTimeManifestStatus.COMPLETED, ScheduledTransitTimeManifestStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledTransitTimeManifestStatus.COMPLETED -> return next == ScheduledTransitTimeManifestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledTransitTimeManifestStatus) {
        if (!canMoveTo(next)) {
            error("scheduled transit time manifest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedVolume(): Double = round((score - capacity.toDouble()) * 100) / 100

    fun snapshot(): ScheduledTransitTimeManifest {
        return ScheduledTransitTimeManifest(
            code = code,
            name = name,
            score = score,
            capacity = capacity,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled transit time manifest in status planned. */
fun newScheduledTransitTimeManifest(code: String, name: String): ScheduledTransitTimeManifest {
    return ScheduledTransitTimeManifest(
        code = code,
        name = name,
        score = 0.0,
        capacity = 0,
        startsAt = 0,
        status = ScheduledTransitTimeManifestStatus.PLANNED,
    )
}
