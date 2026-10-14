package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive lane plan record is in its life cycle. */
enum class AdaptiveLanePlanRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One adaptive lane plan record, with what the planners need to act on it. */
data class AdaptiveLanePlanRecord(
    var code: String,
    var name: String,
    var endsAt: Int,
    var graceDays: Int,
    var externalRef: String,
    var locale: String,
    var distanceKm: Double,
    var tags: MutableList<String>,
    var margin: Double,
    var status: AdaptiveLanePlanRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLanePlanRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLanePlanRecordStatus): Boolean {
        when (status) {
            AdaptiveLanePlanRecordStatus.PROPOSED -> {
                when (next) {
                    AdaptiveLanePlanRecordStatus.APPROVED -> return true
                }
                return false
            }
            AdaptiveLanePlanRecordStatus.APPROVED -> {
                when (next) {
                    AdaptiveLanePlanRecordStatus.REJECTED -> return true
                }
                return false
            }
            AdaptiveLanePlanRecordStatus.REJECTED -> return next == AdaptiveLanePlanRecordStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AdaptiveLanePlanRecordStatus) {
        if (!canMoveTo(next)) {
            error("adaptive lane plan record $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive lane plan record"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round(endsAt.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): AdaptiveLanePlanRecord {
        return AdaptiveLanePlanRecord(
            code = code,
            name = name,
            endsAt = endsAt,
            graceDays = graceDays,
            externalRef = externalRef,
            locale = locale,
            distanceKm = distanceKm,
            tags = tags,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive lane plan record in status proposed. */
fun newAdaptiveLanePlanRecord(code: String, name: String): AdaptiveLanePlanRecord {
    return AdaptiveLanePlanRecord(
        code = code,
        name = name,
        endsAt = 0,
        graceDays = 0,
        externalRef = "",
        locale = "",
        distanceKm = 0.0,
        tags = mutableListOf<String>(),
        margin = 0.0,
        status = AdaptiveLanePlanRecordStatus.PROPOSED,
    )
}
