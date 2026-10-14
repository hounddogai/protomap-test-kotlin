package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss mapping is in its life cycle. */
enum class NearMissMappingStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A near miss mapping as the near miss team keeps it. */
data class NearMissMapping(
    var code: String,
    var title: String,
    var weight: Double,
    var odometer: Int,
    var channel: String,
    var status: NearMissMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissMappingStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissMappingStatus): Boolean {
        when (status) {
            NearMissMappingStatus.PROPOSED -> {
                when (next) {
                    NearMissMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
            NearMissMappingStatus.APPROVED -> return next == NearMissMappingStatus.REJECTED
            NearMissMappingStatus.REJECTED -> return next == NearMissMappingStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: NearMissMappingStatus) {
        if (!canMoveTo(next)) {
            error("near miss mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"near miss mapping"} $code"

    fun projectedValue(): Double = round((weight + odometer.toDouble()) * 100) / 100

    fun snapshot(): NearMissMapping {
        return NearMissMapping(
            code = code,
            title = title,
            weight = weight,
            odometer = odometer,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a near miss mapping in status proposed. */
fun newNearMissMapping(code: String, title: String): NearMissMapping {
    return NearMissMapping(
        code = code,
        title = title,
        weight = 0.0,
        odometer = 0,
        channel = "",
        status = NearMissMappingStatus.PROPOSED,
    )
}
