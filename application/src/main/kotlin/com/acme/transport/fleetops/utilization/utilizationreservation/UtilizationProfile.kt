package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.text.normalize

/** Where a utilization profile is in its life cycle. */
enum class UtilizationProfileStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One utilization profile, with what the planners need to act on it. */
data class UtilizationProfile(
    var code: String,
    var label: String,
    var unit: String,
    var channel: String,
    var country: String,
    var phone: String,
    var discount: Double,
    var lane: String,
    var status: UtilizationProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationProfileStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationProfileStatus): Boolean {
        when (status) {
            UtilizationProfileStatus.PROPOSED -> {
                when (next) {
                    UtilizationProfileStatus.APPROVED, UtilizationProfileStatus.REJECTED -> return true
                }
                return false
            }
            UtilizationProfileStatus.APPROVED -> return next == UtilizationProfileStatus.REJECTED || next == UtilizationProfileStatus.WITHDRAWN
            UtilizationProfileStatus.REJECTED -> return next == UtilizationProfileStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: UtilizationProfileStatus) {
        if (!canMoveTo(next)) {
            error("utilization profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = discount

    fun snapshot(): UtilizationProfile {
        return UtilizationProfile(
            code = code,
            label = label,
            unit = unit,
            channel = channel,
            country = country,
            phone = phone,
            discount = discount,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a utilization profile in status proposed. */
fun newUtilizationProfile(code: String, label: String): UtilizationProfile {
    return UtilizationProfile(
        code = code,
        label = label,
        unit = "",
        channel = "",
        country = "",
        phone = "",
        discount = 0.0,
        lane = "",
        status = UtilizationProfileStatus.PROPOSED,
    )
}
