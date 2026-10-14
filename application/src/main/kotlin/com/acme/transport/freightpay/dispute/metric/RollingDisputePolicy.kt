package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling dispute policy is in its life cycle. */
enum class RollingDisputePolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a rolling dispute policy and its current status. */
data class RollingDisputePolicy(
    var code: String,
    var label: String,
    var visible: Boolean,
    var rate: Double,
    var odometer: Int,
    var country: String,
    var updatedAt: Int,
    var createdAt: Int,
    var ceiling: Double,
    var status: RollingDisputePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDisputePolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDisputePolicyStatus): Boolean {
        when (status) {
            RollingDisputePolicyStatus.CREATED -> return next == RollingDisputePolicyStatus.VALIDATED
            RollingDisputePolicyStatus.VALIDATED -> {
                when (next) {
                    RollingDisputePolicyStatus.PUBLISHED, RollingDisputePolicyStatus.RETIRED -> return true
                }
                return false
            }
            RollingDisputePolicyStatus.PUBLISHED -> {
                when (next) {
                    RollingDisputePolicyStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDisputePolicyStatus) {
        if (!canMoveTo(next)) {
            error("rolling dispute policy $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun netTotal(): Double = round(rate * odometer.toDouble() * 100) / 100

    fun snapshot(): RollingDisputePolicy {
        return RollingDisputePolicy(
            code = code,
            label = label,
            visible = visible,
            rate = rate,
            odometer = odometer,
            country = country,
            updatedAt = updatedAt,
            createdAt = createdAt,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rolling dispute policy in status created. */
fun newRollingDisputePolicy(code: String, label: String): RollingDisputePolicy {
    return RollingDisputePolicy(
        code = code,
        label = label,
        visible = false,
        rate = 0.0,
        odometer = 0,
        country = "",
        updatedAt = 0,
        createdAt = 0,
        ceiling = 0.0,
        status = RollingDisputePolicyStatus.CREATED,
    )
}
