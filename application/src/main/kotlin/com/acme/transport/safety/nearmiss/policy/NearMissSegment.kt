package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.kit.text.normalize

/** Where a near miss segment is in its life cycle. */
enum class NearMissSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A near miss segment. Its code is unique within a tenant. */
data class NearMissSegment(
    var code: String,
    var label: String,
    var endsAt: Int,
    var driverId: String,
    var routeCode: String,
    var region: String,
    var status: NearMissSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissSegmentStatus): Boolean {
        when (status) {
            NearMissSegmentStatus.OPEN -> {
                when (next) {
                    NearMissSegmentStatus.IN_REVIEW, NearMissSegmentStatus.RESOLVED -> return true
                }
                return false
            }
            NearMissSegmentStatus.IN_REVIEW -> {
                when (next) {
                    NearMissSegmentStatus.RESOLVED -> return true
                }
                return false
            }
            NearMissSegmentStatus.RESOLVED -> {
                when (next) {
                    NearMissSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            NearMissSegmentStatus.REOPENED -> {
                when (next) {
                    NearMissSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissSegmentStatus) {
        if (!canMoveTo(next)) {
            error("near miss segment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): NearMissSegment {
        return NearMissSegment(
            code = code,
            label = label,
            endsAt = endsAt,
            driverId = driverId,
            routeCode = routeCode,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a near miss segment in status open. */
fun newNearMissSegment(code: String, label: String): NearMissSegment {
    return NearMissSegment(
        code = code,
        label = label,
        endsAt = 0,
        driverId = "",
        routeCode = "",
        region = "",
        status = NearMissSegmentStatus.OPEN,
    )
}
