package com.acme.transport.linehaul.trailer.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer digest is in its life cycle. */
enum class TrailerDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A trailer digest as the trailer team keeps it. */
data class TrailerDigest(
    var code: String,
    var title: String,
    var version: Int,
    var checksum: String,
    var shiftCode: String,
    var vehicleId: String,
    var threshold: Double,
    var routeCode: String,
    var graceDays: Int,
    var createdAt: Int,
    var active: Boolean,
    var status: TrailerDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerDigestStatus): Boolean {
        when (status) {
            TrailerDigestStatus.PLANNED -> return next == TrailerDigestStatus.IN_PROGRESS
            TrailerDigestStatus.SCHEDULED -> {
                when (next) {
                    TrailerDigestStatus.COMPLETED -> return true
                }
                return false
            }
            TrailerDigestStatus.IN_PROGRESS -> {
                when (next) {
                    TrailerDigestStatus.COMPLETED, TrailerDigestStatus.CANCELLED -> return true
                }
                return false
            }
            TrailerDigestStatus.COMPLETED -> {
                when (next) {
                    TrailerDigestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerDigestStatus) {
        if (!canMoveTo(next)) {
            error("trailer digest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedValue(): Double = round((graceDays.toDouble() + threshold) * 100) / 100

    fun snapshot(): TrailerDigest {
        return TrailerDigest(
            code = code,
            title = title,
            version = version,
            checksum = checksum,
            shiftCode = shiftCode,
            vehicleId = vehicleId,
            threshold = threshold,
            routeCode = routeCode,
            graceDays = graceDays,
            createdAt = createdAt,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a trailer digest in status planned. */
fun newTrailerDigest(code: String, title: String): TrailerDigest {
    return TrailerDigest(
        code = code,
        title = title,
        version = 0,
        checksum = "",
        shiftCode = "",
        vehicleId = "",
        threshold = 0.0,
        routeCode = "",
        graceDays = 0,
        createdAt = 0,
        active = false,
        status = TrailerDigestStatus.PLANNED,
    )
}
