package com.acme.transport.crewing.availability.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability manifest is in its life cycle. */
enum class AvailabilityManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One availability manifest, with what the planners need to act on it. */
data class AvailabilityManifest(
    var code: String,
    var name: String,
    var minimum: Double,
    var attempts: Int,
    var currency: String,
    var checksum: String,
    var status: AvailabilityManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityManifestStatus): Boolean {
        when (status) {
            AvailabilityManifestStatus.IDLE -> {
                when (next) {
                    AvailabilityManifestStatus.READY -> return true
                }
                return false
            }
            AvailabilityManifestStatus.WARMING -> return next == AvailabilityManifestStatus.DRAINING || next == AvailabilityManifestStatus.STOPPED
            AvailabilityManifestStatus.READY -> return next == AvailabilityManifestStatus.STOPPED
            AvailabilityManifestStatus.DRAINING -> {
                when (next) {
                    AvailabilityManifestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityManifestStatus) {
        if (!canMoveTo(next)) {
            error("availability manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"availability manifest"} $code"

    fun weightedVolume(): Double = round((attempts.toDouble() + minimum) * 100) / 100

    fun snapshot(): AvailabilityManifest {
        return AvailabilityManifest(
            code = code,
            name = name,
            minimum = minimum,
            attempts = attempts,
            currency = currency,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a availability manifest in status idle. */
fun newAvailabilityManifest(code: String, name: String): AvailabilityManifest {
    return AvailabilityManifest(
        code = code,
        name = name,
        minimum = 0.0,
        attempts = 0,
        currency = "",
        checksum = "",
        status = AvailabilityManifestStatus.IDLE,
    )
}
