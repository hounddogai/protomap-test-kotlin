package com.acme.transport.freightpay.ratecard.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rate card score records of freightpay's rate card work, and the rules, stores, and calculations around
// them.

/** Where a rate card score is in its life cycle. */
enum class RateCardScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rate card score. Its code is unique within a tenant. */
data class RateCardScore(
    var code: String,
    var label: String,
    var dock: String,
    var carrier: String,
    var approved: Boolean,
    var limit: Int,
    var depot: String,
    var odometer: Int,
    var contactEmail: String,
    var tags: MutableList<String>,
    var score: Double,
    var status: RateCardScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardScoreStatus): Boolean {
        when (status) {
            RateCardScoreStatus.OPEN -> return next == RateCardScoreStatus.RESOLVED || next == RateCardScoreStatus.REOPENED
            RateCardScoreStatus.IN_REVIEW -> {
                when (next) {
                    RateCardScoreStatus.ARCHIVED -> return true
                }
                return false
            }
            RateCardScoreStatus.RESOLVED -> {
                when (next) {
                    RateCardScoreStatus.REOPENED -> return true
                }
                return false
            }
            RateCardScoreStatus.REOPENED -> return next == RateCardScoreStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RateCardScoreStatus) {
        if (!canMoveTo(next)) {
            error("rate card score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rate card score"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveExposure(): Double = round((odometer.toDouble() + score) * 100) / 100

    fun snapshot(): RateCardScore {
        return RateCardScore(
            code = code,
            label = label,
            dock = dock,
            carrier = carrier,
            approved = approved,
            limit = limit,
            depot = depot,
            odometer = odometer,
            contactEmail = contactEmail,
            tags = tags,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rate card score in status open. */
fun newRateCardScore(code: String, label: String): RateCardScore {
    return RateCardScore(
        code = code,
        label = label,
        dock = "",
        carrier = "",
        approved = false,
        limit = 0,
        depot = "",
        odometer = 0,
        contactEmail = "",
        tags = mutableListOf<String>(),
        score = 0.0,
        status = RateCardScoreStatus.OPEN,
    )
}
