package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss request is in its life cycle. */
enum class NearMissRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One near miss request, with what the planners need to act on it. */
data class NearMissRequest(
    var code: String,
    var label: String,
    var title: String,
    var lane: String,
    var loadKg: Double,
    var durationMinutes: Int,
    var tags: MutableList<String>,
    var odometer: Int,
    var status: NearMissRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissRequestStatus): Boolean {
        when (status) {
            NearMissRequestStatus.REQUESTED -> {
                when (next) {
                    NearMissRequestStatus.GRANTED, NearMissRequestStatus.REVOKED -> return true
                }
                return false
            }
            NearMissRequestStatus.GRANTED -> return next == NearMissRequestStatus.EXPIRED
            NearMissRequestStatus.REVOKED -> {
                when (next) {
                    NearMissRequestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissRequestStatus) {
        if (!canMoveTo(next)) {
            error("near miss request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round((odometer.toDouble() - loadKg) * 100) / 100

    fun snapshot(): NearMissRequest {
        return NearMissRequest(
            code = code,
            label = label,
            title = title,
            lane = lane,
            loadKg = loadKg,
            durationMinutes = durationMinutes,
            tags = tags,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a near miss request in status requested. */
fun newNearMissRequest(code: String, label: String): NearMissRequest {
    return NearMissRequest(
        code = code,
        label = label,
        title = "",
        lane = "",
        loadKg = 0.0,
        durationMinutes = 0,
        tags = mutableListOf<String>(),
        odometer = 0,
        status = NearMissRequestStatus.REQUESTED,
    )
}
