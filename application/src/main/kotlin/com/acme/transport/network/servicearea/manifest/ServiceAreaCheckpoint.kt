package com.acme.transport.network.servicearea.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area checkpoint is in its life cycle. */
enum class ServiceAreaCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One service area checkpoint, with what the planners need to act on it. */
data class ServiceAreaCheckpoint(
    var code: String,
    var label: String,
    var sequence: Int,
    var ratio: Double,
    var source: String,
    var expiresAt: Int,
    var durationMinutes: Int,
    var status: ServiceAreaCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaCheckpointStatus): Boolean {
        when (status) {
            ServiceAreaCheckpointStatus.PROPOSED -> return next == ServiceAreaCheckpointStatus.REJECTED
            ServiceAreaCheckpointStatus.APPROVED -> {
                when (next) {
                    ServiceAreaCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
            ServiceAreaCheckpointStatus.REJECTED -> {
                when (next) {
                    ServiceAreaCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("service area checkpoint $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"service area checkpoint"} $code"

    fun effectiveVolume(): Double = round((ratio - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ServiceAreaCheckpoint {
        return ServiceAreaCheckpoint(
            code = code,
            label = label,
            sequence = sequence,
            ratio = ratio,
            source = source,
            expiresAt = expiresAt,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a service area checkpoint in status proposed. */
fun newServiceAreaCheckpoint(code: String, label: String): ServiceAreaCheckpoint {
    return ServiceAreaCheckpoint(
        code = code,
        label = label,
        sequence = 0,
        ratio = 0.0,
        source = "",
        expiresAt = 0,
        durationMinutes = 0,
        status = ServiceAreaCheckpointStatus.PROPOSED,
    )
}
