package com.acme.transport.safety.incident.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The incident digest records of safety's incident work, and the rules, stores, and calculations around them.

/** Where a incident digest is in its life cycle. */
enum class IncidentDigestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One incident digest, with what the planners need to act on it. */
data class IncidentDigest(
    var code: String,
    var title: String,
    var timezone: String,
    var lane: String,
    var currency: String,
    var loadKg: Double,
    var revision: Int,
    var trailerId: String,
    var summary: String,
    var archived: Boolean,
    var driverId: String,
    var status: IncidentDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentDigestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentDigestStatus): Boolean {
        when (status) {
            IncidentDigestStatus.CREATED -> return next == IncidentDigestStatus.VALIDATED
            IncidentDigestStatus.VALIDATED -> {
                when (next) {
                    IncidentDigestStatus.RETIRED -> return true
                }
                return false
            }
            IncidentDigestStatus.PUBLISHED -> {
                when (next) {
                    IncidentDigestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentDigestStatus) {
        if (!canMoveTo(next)) {
            error("incident digest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"incident digest"} $code"

    fun adjustedLoad(): Double = round(loadKg * revision.toDouble() * 100) / 100

    fun snapshot(): IncidentDigest {
        return IncidentDigest(
            code = code,
            title = title,
            timezone = timezone,
            lane = lane,
            currency = currency,
            loadKg = loadKg,
            revision = revision,
            trailerId = trailerId,
            summary = summary,
            archived = archived,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a incident digest in status created. */
fun newIncidentDigest(code: String, title: String): IncidentDigest {
    return IncidentDigest(
        code = code,
        title = title,
        timezone = "",
        lane = "",
        currency = "",
        loadKg = 0.0,
        revision = 0,
        trailerId = "",
        summary = "",
        archived = false,
        driverId = "",
        status = IncidentDigestStatus.CREATED,
    )
}
