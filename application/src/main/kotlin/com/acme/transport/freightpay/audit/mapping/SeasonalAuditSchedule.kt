package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.kit.text.normalize

/** Where a seasonal audit schedule is in its life cycle. */
enum class SeasonalAuditScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal audit schedule. Its code is unique within a tenant. */
data class SeasonalAuditSchedule(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var reviewer: String,
    var distanceKm: Double,
    var status: SeasonalAuditScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAuditScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAuditScheduleStatus): Boolean {
        when (status) {
            SeasonalAuditScheduleStatus.DRAFT -> {
                when (next) {
                    SeasonalAuditScheduleStatus.PENDING -> return true
                }
                return false
            }
            SeasonalAuditScheduleStatus.PENDING -> {
                when (next) {
                    SeasonalAuditScheduleStatus.SUSPENDED, SeasonalAuditScheduleStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalAuditScheduleStatus.ACTIVE -> {
                when (next) {
                    SeasonalAuditScheduleStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalAuditScheduleStatus.SUSPENDED -> return next == SeasonalAuditScheduleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SeasonalAuditScheduleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal audit schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal audit schedule"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = distanceKm

    fun snapshot(): SeasonalAuditSchedule {
        return SeasonalAuditSchedule(
            code = code,
            title = title,
            tags = tags,
            reviewer = reviewer,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal audit schedule in status draft. */
fun newSeasonalAuditSchedule(code: String, title: String): SeasonalAuditSchedule {
    return SeasonalAuditSchedule(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        reviewer = "",
        distanceKm = 0.0,
        status = SeasonalAuditScheduleStatus.DRAFT,
    )
}
