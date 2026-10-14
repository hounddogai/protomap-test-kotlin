package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.text.normalize

/** Where a lane plan quota is in its life cycle. */
enum class LanePlanQuotaStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a lane plan quota and its current status. */
data class LanePlanQuota(
    var code: String,
    var name: String,
    var quantity: Int,
    var timezone: String,
    var externalRef: String,
    var status: LanePlanQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanQuotaStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanQuotaStatus): Boolean {
        when (status) {
            LanePlanQuotaStatus.PLANNED -> {
                when (next) {
                    LanePlanQuotaStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LanePlanQuotaStatus.SCHEDULED -> return next == LanePlanQuotaStatus.IN_PROGRESS || next == LanePlanQuotaStatus.COMPLETED
            LanePlanQuotaStatus.IN_PROGRESS -> {
                when (next) {
                    LanePlanQuotaStatus.COMPLETED, LanePlanQuotaStatus.CANCELLED -> return true
                }
                return false
            }
            LanePlanQuotaStatus.COMPLETED -> {
                when (next) {
                    LanePlanQuotaStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanQuotaStatus) {
        if (!canMoveTo(next)) {
            error("lane plan quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): LanePlanQuota {
        return LanePlanQuota(
            code = code,
            name = name,
            quantity = quantity,
            timezone = timezone,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lane plan quota in status planned. */
fun newLanePlanQuota(code: String, name: String): LanePlanQuota {
    return LanePlanQuota(
        code = code,
        name = name,
        quantity = 0,
        timezone = "",
        externalRef = "",
        status = LanePlanQuotaStatus.PLANNED,
    )
}
