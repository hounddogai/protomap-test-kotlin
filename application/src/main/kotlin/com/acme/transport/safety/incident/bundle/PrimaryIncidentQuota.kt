package com.acme.transport.safety.incident.bundle

import com.acme.transport.kit.text.normalize

/** Where a primary incident quota is in its life cycle. */
enum class PrimaryIncidentQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A primary incident quota as the incident team keeps it. */
data class PrimaryIncidentQuota(
    var code: String,
    var name: String,
    var externalRef: String,
    var lane: String,
    var segment: String,
    var reviewer: String,
    var dock: String,
    var source: String,
    var amount: Double,
    var status: PrimaryIncidentQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryIncidentQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryIncidentQuotaStatus): Boolean {
        when (status) {
            PrimaryIncidentQuotaStatus.OPEN -> return next == PrimaryIncidentQuotaStatus.RESOLVED || next == PrimaryIncidentQuotaStatus.REOPENED
            PrimaryIncidentQuotaStatus.IN_REVIEW -> return next == PrimaryIncidentQuotaStatus.RESOLVED
            PrimaryIncidentQuotaStatus.RESOLVED -> {
                when (next) {
                    PrimaryIncidentQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryIncidentQuotaStatus.REOPENED -> return next == PrimaryIncidentQuotaStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryIncidentQuotaStatus) {
        if (!canMoveTo(next)) {
            error("primary incident quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary incident quota"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): PrimaryIncidentQuota {
        return PrimaryIncidentQuota(
            code = code,
            name = name,
            externalRef = externalRef,
            lane = lane,
            segment = segment,
            reviewer = reviewer,
            dock = dock,
            source = source,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a primary incident quota in status open. */
fun newPrimaryIncidentQuota(code: String, name: String): PrimaryIncidentQuota {
    return PrimaryIncidentQuota(
        code = code,
        name = name,
        externalRef = "",
        lane = "",
        segment = "",
        reviewer = "",
        dock = "",
        source = "",
        amount = 0.0,
        status = PrimaryIncidentQuotaStatus.OPEN,
    )
}
