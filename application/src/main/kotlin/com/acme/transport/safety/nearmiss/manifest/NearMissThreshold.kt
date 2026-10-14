package com.acme.transport.safety.nearmiss.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss threshold is in its life cycle. */
enum class NearMissThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One near miss threshold, with what the planners need to act on it. */
data class NearMissThreshold(
    var code: String,
    var title: String,
    var rate: Double,
    var shiftCode: String,
    var sequence: Int,
    var startsAt: Int,
    var unit: String,
    var status: NearMissThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissThresholdStatus): Boolean {
        when (status) {
            NearMissThresholdStatus.REQUESTED -> return next == NearMissThresholdStatus.REVOKED
            NearMissThresholdStatus.GRANTED -> {
                when (next) {
                    NearMissThresholdStatus.REVOKED, NearMissThresholdStatus.EXPIRED -> return true
                }
                return false
            }
            NearMissThresholdStatus.REVOKED -> {
                when (next) {
                    NearMissThresholdStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissThresholdStatus) {
        if (!canMoveTo(next)) {
            error("near miss threshold $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveVolume(): Double = round(startsAt.toDouble() * rate * 100) / 100

    fun snapshot(): NearMissThreshold {
        return NearMissThreshold(
            code = code,
            title = title,
            rate = rate,
            shiftCode = shiftCode,
            sequence = sequence,
            startsAt = startsAt,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a near miss threshold in status requested. */
fun newNearMissThreshold(code: String, title: String): NearMissThreshold {
    return NearMissThreshold(
        code = code,
        title = title,
        rate = 0.0,
        shiftCode = "",
        sequence = 0,
        startsAt = 0,
        unit = "",
        status = NearMissThresholdStatus.REQUESTED,
    )
}
