package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global service plan digest is in its life cycle. */
enum class GlobalServicePlanDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One global service plan digest, with what the planners need to act on it. */
data class GlobalServicePlanDigest(
    var code: String,
    var title: String,
    var score: Double,
    var threshold: Double,
    var minimum: Double,
    var endsAt: Int,
    var lane: String,
    var timezone: String,
    var approved: Boolean,
    var status: GlobalServicePlanDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalServicePlanDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalServicePlanDigestStatus): Boolean {
        when (status) {
            GlobalServicePlanDigestStatus.REQUESTED -> {
                when (next) {
                    GlobalServicePlanDigestStatus.GRANTED, GlobalServicePlanDigestStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalServicePlanDigestStatus.GRANTED -> {
                when (next) {
                    GlobalServicePlanDigestStatus.REVOKED, GlobalServicePlanDigestStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalServicePlanDigestStatus.REVOKED -> return next == GlobalServicePlanDigestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalServicePlanDigestStatus) {
        if (!canMoveTo(next)) {
            error("global service plan digest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedVolume(): Double = round((endsAt.toDouble() - minimum) * 100) / 100

    fun snapshot(): GlobalServicePlanDigest {
        return GlobalServicePlanDigest(
            code = code,
            title = title,
            score = score,
            threshold = threshold,
            minimum = minimum,
            endsAt = endsAt,
            lane = lane,
            timezone = timezone,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global service plan digest in status requested. */
fun newGlobalServicePlanDigest(code: String, title: String): GlobalServicePlanDigest {
    return GlobalServicePlanDigest(
        code = code,
        title = title,
        score = 0.0,
        threshold = 0.0,
        minimum = 0.0,
        endsAt = 0,
        lane = "",
        timezone = "",
        approved = false,
        status = GlobalServicePlanDigestStatus.REQUESTED,
    )
}
