package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.kit.text.normalize

/** Where a adaptive duty period bundle is in its life cycle. */
enum class AdaptiveDutyPeriodBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive duty period bundle as the duty period team keeps it. */
data class AdaptiveDutyPeriodBundle(
    var code: String,
    var name: String,
    var visible: Boolean,
    var amount: Double,
    var timezone: String,
    var routeCode: String,
    var status: AdaptiveDutyPeriodBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDutyPeriodBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDutyPeriodBundleStatus): Boolean {
        when (status) {
            AdaptiveDutyPeriodBundleStatus.REQUESTED -> {
                when (next) {
                    AdaptiveDutyPeriodBundleStatus.REVOKED -> return true
                }
                return false
            }
            AdaptiveDutyPeriodBundleStatus.GRANTED -> {
                when (next) {
                    AdaptiveDutyPeriodBundleStatus.REVOKED, AdaptiveDutyPeriodBundleStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveDutyPeriodBundleStatus.REVOKED -> {
                when (next) {
                    AdaptiveDutyPeriodBundleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDutyPeriodBundleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive duty period bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = amount

    fun snapshot(): AdaptiveDutyPeriodBundle {
        return AdaptiveDutyPeriodBundle(
            code = code,
            name = name,
            visible = visible,
            amount = amount,
            timezone = timezone,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive duty period bundle in status requested. */
fun newAdaptiveDutyPeriodBundle(code: String, name: String): AdaptiveDutyPeriodBundle {
    return AdaptiveDutyPeriodBundle(
        code = code,
        name = name,
        visible = false,
        amount = 0.0,
        timezone = "",
        routeCode = "",
        status = AdaptiveDutyPeriodBundleStatus.REQUESTED,
    )
}
