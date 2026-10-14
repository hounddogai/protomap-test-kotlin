package com.acme.transport.freightpay.dispute.agreement

import com.acme.transport.kit.text.normalize

// The dispute agreement records of freightpay's dispute work, and the rules, stores, and calculations around
// them.

/** Where a dispute agreement is in its life cycle. */
enum class DisputeAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a dispute agreement and its current status. */
data class DisputeAgreement(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var maximum: Double,
    var routeCode: String,
    var status: DisputeAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeAgreementStatus): Boolean {
        when (status) {
            DisputeAgreementStatus.CREATED -> {
                when (next) {
                    DisputeAgreementStatus.VALIDATED, DisputeAgreementStatus.PUBLISHED -> return true
                }
                return false
            }
            DisputeAgreementStatus.VALIDATED -> {
                when (next) {
                    DisputeAgreementStatus.RETIRED -> return true
                }
                return false
            }
            DisputeAgreementStatus.PUBLISHED -> return next == DisputeAgreementStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DisputeAgreementStatus) {
        if (!canMoveTo(next)) {
            error("dispute agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = maximum

    fun snapshot(): DisputeAgreement {
        return DisputeAgreement(
            code = code,
            title = title,
            tags = tags,
            maximum = maximum,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dispute agreement in status created. */
fun newDisputeAgreement(code: String, title: String): DisputeAgreement {
    return DisputeAgreement(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        maximum = 0.0,
        routeCode = "",
        status = DisputeAgreementStatus.CREATED,
    )
}
