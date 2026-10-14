package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling near miss mapping is in its life cycle. */
enum class RollingNearMissMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One rolling near miss mapping, with what the planners need to act on it. */
data class RollingNearMissMapping(
    var code: String,
    var label: String,
    var endsAt: Int,
    var amount: Double,
    var dock: String,
    var country: String,
    var durationMinutes: Int,
    var channel: String,
    var status: RollingNearMissMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingNearMissMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingNearMissMappingStatus): Boolean {
        when (status) {
            RollingNearMissMappingStatus.PLANNED -> {
                when (next) {
                    RollingNearMissMappingStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RollingNearMissMappingStatus.SCHEDULED -> {
                when (next) {
                    RollingNearMissMappingStatus.IN_PROGRESS, RollingNearMissMappingStatus.COMPLETED -> return true
                }
                return false
            }
            RollingNearMissMappingStatus.IN_PROGRESS -> {
                when (next) {
                    RollingNearMissMappingStatus.COMPLETED, RollingNearMissMappingStatus.CANCELLED -> return true
                }
                return false
            }
            RollingNearMissMappingStatus.COMPLETED -> {
                when (next) {
                    RollingNearMissMappingStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingNearMissMappingStatus) {
        if (!canMoveTo(next)) {
            error("rolling near miss mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedLoad(): Double = round(endsAt.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): RollingNearMissMapping {
        return RollingNearMissMapping(
            code = code,
            label = label,
            endsAt = endsAt,
            amount = amount,
            dock = dock,
            country = country,
            durationMinutes = durationMinutes,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rolling near miss mapping in status planned. */
fun newRollingNearMissMapping(code: String, label: String): RollingNearMissMapping {
    return RollingNearMissMapping(
        code = code,
        label = label,
        endsAt = 0,
        amount = 0.0,
        dock = "",
        country = "",
        durationMinutes = 0,
        channel = "",
        status = RollingNearMissMappingStatus.PLANNED,
    )
}
