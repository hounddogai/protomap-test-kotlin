package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.text.normalize

/** Where a seasonal service area quota is in its life cycle. */
enum class SeasonalServiceAreaQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal service area quota. Its code is unique within a tenant. */
data class SeasonalServiceAreaQuota(
    var code: String,
    var label: String,
    var externalRef: String,
    var segment: String,
    var version: Int,
    var name: String,
    var phone: String,
    var status: SeasonalServiceAreaQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalServiceAreaQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalServiceAreaQuotaStatus): Boolean {
        when (status) {
            SeasonalServiceAreaQuotaStatus.OPEN -> {
                when (next) {
                    SeasonalServiceAreaQuotaStatus.IN_REVIEW -> return true
                }
                return false
            }
            SeasonalServiceAreaQuotaStatus.IN_REVIEW -> return next == SeasonalServiceAreaQuotaStatus.RESOLVED || next == SeasonalServiceAreaQuotaStatus.REOPENED
            SeasonalServiceAreaQuotaStatus.RESOLVED -> return next == SeasonalServiceAreaQuotaStatus.ARCHIVED
            SeasonalServiceAreaQuotaStatus.REOPENED -> {
                when (next) {
                    SeasonalServiceAreaQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalServiceAreaQuotaStatus) {
        if (!canMoveTo(next)) {
            error("seasonal service area quota $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): SeasonalServiceAreaQuota {
        return SeasonalServiceAreaQuota(
            code = code,
            label = label,
            externalRef = externalRef,
            segment = segment,
            version = version,
            name = name,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a seasonal service area quota in status open. */
fun newSeasonalServiceAreaQuota(code: String, label: String): SeasonalServiceAreaQuota {
    return SeasonalServiceAreaQuota(
        code = code,
        label = label,
        externalRef = "",
        segment = "",
        version = 0,
        name = "",
        phone = "",
        status = SeasonalServiceAreaQuotaStatus.OPEN,
    )
}
