package com.acme.transport.linehaul.capacity.segment

import com.acme.transport.kit.text.normalize

/** Where a global capacity agreement is in its life cycle. */
enum class GlobalCapacityAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One global capacity agreement, with what the planners need to act on it. */
data class GlobalCapacityAgreement(
    var code: String,
    var title: String,
    var depot: String,
    var timezone: String,
    var weight: Double,
    var trailerId: String,
    var tags: MutableList<String>,
    var region: String,
    var pinned: Boolean,
    var status: GlobalCapacityAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCapacityAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCapacityAgreementStatus): Boolean {
        when (status) {
            GlobalCapacityAgreementStatus.PLANNED -> {
                when (next) {
                    GlobalCapacityAgreementStatus.SCHEDULED -> return true
                }
                return false
            }
            GlobalCapacityAgreementStatus.SCHEDULED -> return next == GlobalCapacityAgreementStatus.COMPLETED
            GlobalCapacityAgreementStatus.IN_PROGRESS -> return next == GlobalCapacityAgreementStatus.COMPLETED || next == GlobalCapacityAgreementStatus.CANCELLED
            GlobalCapacityAgreementStatus.COMPLETED -> {
                when (next) {
                    GlobalCapacityAgreementStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCapacityAgreementStatus) {
        if (!canMoveTo(next)) {
            error("global capacity agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = weight

    fun snapshot(): GlobalCapacityAgreement {
        return GlobalCapacityAgreement(
            code = code,
            title = title,
            depot = depot,
            timezone = timezone,
            weight = weight,
            trailerId = trailerId,
            tags = tags,
            region = region,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global capacity agreement in status planned. */
fun newGlobalCapacityAgreement(code: String, title: String): GlobalCapacityAgreement {
    return GlobalCapacityAgreement(
        code = code,
        title = title,
        depot = "",
        timezone = "",
        weight = 0.0,
        trailerId = "",
        tags = mutableListOf<String>(),
        region = "",
        pinned = false,
        status = GlobalCapacityAgreementStatus.PLANNED,
    )
}
