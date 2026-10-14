package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default drop agreement is in its life cycle. */
enum class DefaultDropAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One default drop agreement, with what the planners need to act on it. */
data class DefaultDropAgreement(
    var code: String,
    var title: String,
    var budget: Double,
    var attributes: MutableMap<String, String>,
    var odometer: Int,
    var checksum: String,
    var reviewer: String,
    var lane: String,
    var capacity: Int,
    var contactEmail: String,
    var status: DefaultDropAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDropAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDropAgreementStatus): Boolean {
        when (status) {
            DefaultDropAgreementStatus.OPEN -> return next == DefaultDropAgreementStatus.IN_REVIEW
            DefaultDropAgreementStatus.IN_REVIEW -> {
                when (next) {
                    DefaultDropAgreementStatus.REOPENED -> return true
                }
                return false
            }
            DefaultDropAgreementStatus.RESOLVED -> {
                when (next) {
                    DefaultDropAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultDropAgreementStatus.REOPENED -> {
                when (next) {
                    DefaultDropAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDropAgreementStatus) {
        if (!canMoveTo(next)) {
            error("default drop agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default drop agreement"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round(capacity.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): DefaultDropAgreement {
        return DefaultDropAgreement(
            code = code,
            title = title,
            budget = budget,
            attributes = attributes,
            odometer = odometer,
            checksum = checksum,
            reviewer = reviewer,
            lane = lane,
            capacity = capacity,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default drop agreement in status open. */
fun newDefaultDropAgreement(code: String, title: String): DefaultDropAgreement {
    return DefaultDropAgreement(
        code = code,
        title = title,
        budget = 0.0,
        attributes = mutableMapOf<String, String>(),
        odometer = 0,
        checksum = "",
        reviewer = "",
        lane = "",
        capacity = 0,
        contactEmail = "",
        status = DefaultDropAgreementStatus.OPEN,
    )
}
