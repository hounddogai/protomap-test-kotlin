package com.acme.transport.crewing.roster.review

import com.acme.transport.kit.text.normalize

/** Where a regional roster quota is in its life cycle. */
enum class RegionalRosterQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A regional roster quota. Its code is unique within a tenant. */
data class RegionalRosterQuota(
    var code: String,
    var title: String,
    var active: Boolean,
    var channel: String,
    var currency: String,
    var quantity: Int,
    var status: RegionalRosterQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRosterQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRosterQuotaStatus): Boolean {
        when (status) {
            RegionalRosterQuotaStatus.REQUESTED -> return next == RegionalRosterQuotaStatus.REVOKED || next == RegionalRosterQuotaStatus.EXPIRED
            RegionalRosterQuotaStatus.GRANTED -> {
                when (next) {
                    RegionalRosterQuotaStatus.REVOKED, RegionalRosterQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalRosterQuotaStatus.REVOKED -> return next == RegionalRosterQuotaStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalRosterQuotaStatus) {
        if (!canMoveTo(next)) {
            error("regional roster quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): RegionalRosterQuota {
        return RegionalRosterQuota(
            code = code,
            title = title,
            active = active,
            channel = channel,
            currency = currency,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional roster quota in status requested. */
fun newRegionalRosterQuota(code: String, title: String): RegionalRosterQuota {
    return RegionalRosterQuota(
        code = code,
        title = title,
        active = false,
        channel = "",
        currency = "",
        quantity = 0,
        status = RegionalRosterQuotaStatus.REQUESTED,
    )
}
