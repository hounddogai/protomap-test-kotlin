package com.acme.transport.linehaul.laneplan.quota

import com.acme.transport.kit.text.normalize

// The scheduled lane plan quota records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a scheduled lane plan quota is in its life cycle. */
enum class ScheduledLanePlanQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled lane plan quota. Its code is unique within a tenant. */
data class ScheduledLanePlanQuota(
    var code: String,
    var label: String,
    var description: String,
    var contactEmail: String,
    var revision: Int,
    var region: String,
    var name: String,
    var depot: String,
    var status: ScheduledLanePlanQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLanePlanQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLanePlanQuotaStatus): Boolean {
        when (status) {
            ScheduledLanePlanQuotaStatus.NEW -> {
                when (next) {
                    ScheduledLanePlanQuotaStatus.RUNNING, ScheduledLanePlanQuotaStatus.FAILED -> return true
                }
                return false
            }
            ScheduledLanePlanQuotaStatus.QUEUED -> return next == ScheduledLanePlanQuotaStatus.DONE || next == ScheduledLanePlanQuotaStatus.FAILED
            ScheduledLanePlanQuotaStatus.RUNNING -> {
                when (next) {
                    ScheduledLanePlanQuotaStatus.DONE, ScheduledLanePlanQuotaStatus.FAILED -> return true
                }
                return false
            }
            ScheduledLanePlanQuotaStatus.DONE -> return next == ScheduledLanePlanQuotaStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledLanePlanQuotaStatus) {
        if (!canMoveTo(next)) {
            error("scheduled lane plan quota $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): ScheduledLanePlanQuota {
        return ScheduledLanePlanQuota(
            code = code,
            label = label,
            description = description,
            contactEmail = contactEmail,
            revision = revision,
            region = region,
            name = name,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled lane plan quota in status new. */
fun newScheduledLanePlanQuota(code: String, label: String): ScheduledLanePlanQuota {
    return ScheduledLanePlanQuota(
        code = code,
        label = label,
        description = "",
        contactEmail = "",
        revision = 0,
        region = "",
        name = "",
        depot = "",
        status = ScheduledLanePlanQuotaStatus.NEW,
    )
}
