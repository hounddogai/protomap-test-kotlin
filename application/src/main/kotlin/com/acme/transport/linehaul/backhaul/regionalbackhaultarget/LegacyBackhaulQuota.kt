package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.text.normalize

/** Where a legacy backhaul quota is in its life cycle. */
enum class LegacyBackhaulQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One legacy backhaul quota, with what the planners need to act on it. */
data class LegacyBackhaulQuota(
    var code: String,
    var title: String,
    var cost: Double,
    var label: String,
    var unit: String,
    var phone: String,
    var status: LegacyBackhaulQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyBackhaulQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyBackhaulQuotaStatus): Boolean {
        when (status) {
            LegacyBackhaulQuotaStatus.PROPOSED -> {
                when (next) {
                    LegacyBackhaulQuotaStatus.REJECTED, LegacyBackhaulQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyBackhaulQuotaStatus.APPROVED -> return next == LegacyBackhaulQuotaStatus.REJECTED
            LegacyBackhaulQuotaStatus.REJECTED -> {
                when (next) {
                    LegacyBackhaulQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyBackhaulQuotaStatus) {
        if (!canMoveTo(next)) {
            error("legacy backhaul quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy backhaul quota"} $code"

    fun measuredValue(): Double = cost

    fun snapshot(): LegacyBackhaulQuota {
        return LegacyBackhaulQuota(
            code = code,
            title = title,
            cost = cost,
            label = label,
            unit = unit,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a legacy backhaul quota in status proposed. */
fun newLegacyBackhaulQuota(code: String, title: String): LegacyBackhaulQuota {
    return LegacyBackhaulQuota(
        code = code,
        title = title,
        cost = 0.0,
        label = "",
        unit = "",
        phone = "",
        status = LegacyBackhaulQuotaStatus.PROPOSED,
    )
}
