package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary lane plan request records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a primary lane plan request is in its life cycle. */
enum class PrimaryLanePlanRequestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary lane plan request, with what the planners need to act on it. */
data class PrimaryLanePlanRequest(
    var code: String,
    var name: String,
    var country: String,
    var contactEmail: String,
    var budget: Double,
    var graceDays: Int,
    var pinned: Boolean,
    var source: String,
    var region: String,
    var channel: String,
    var status: PrimaryLanePlanRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryLanePlanRequestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryLanePlanRequestStatus): Boolean {
        when (status) {
            PrimaryLanePlanRequestStatus.OPEN -> {
                when (next) {
                    PrimaryLanePlanRequestStatus.RESOLVED, PrimaryLanePlanRequestStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryLanePlanRequestStatus.IN_REVIEW -> return next == PrimaryLanePlanRequestStatus.ARCHIVED
            PrimaryLanePlanRequestStatus.RESOLVED -> return next == PrimaryLanePlanRequestStatus.REOPENED
            PrimaryLanePlanRequestStatus.REOPENED -> {
                when (next) {
                    PrimaryLanePlanRequestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryLanePlanRequestStatus) {
        if (!canMoveTo(next)) {
            error("primary lane plan request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary lane plan request"} $code"

    fun adjustedLoad(): Double = round((graceDays.toDouble() - budget) * 100) / 100

    fun snapshot(): PrimaryLanePlanRequest {
        return PrimaryLanePlanRequest(
            code = code,
            name = name,
            country = country,
            contactEmail = contactEmail,
            budget = budget,
            graceDays = graceDays,
            pinned = pinned,
            source = source,
            region = region,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary lane plan request in status open. */
fun newPrimaryLanePlanRequest(code: String, name: String): PrimaryLanePlanRequest {
    return PrimaryLanePlanRequest(
        code = code,
        name = name,
        country = "",
        contactEmail = "",
        budget = 0.0,
        graceDays = 0,
        pinned = false,
        source = "",
        region = "",
        channel = "",
        status = PrimaryLanePlanRequestStatus.OPEN,
    )
}
