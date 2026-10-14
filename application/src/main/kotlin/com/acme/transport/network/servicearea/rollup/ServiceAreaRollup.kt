package com.acme.transport.network.servicearea.rollup

import com.acme.transport.kit.text.normalize

// The service area rollup records of network's service area work, and the rules, stores, and calculations
// around them.

/** Where a service area rollup is in its life cycle. */
enum class ServiceAreaRollupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One service area rollup, with what the planners need to act on it. */
data class ServiceAreaRollup(
    var code: String,
    var label: String,
    var title: String,
    var graceDays: Int,
    var currency: String,
    var status: ServiceAreaRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaRollupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaRollupStatus): Boolean {
        when (status) {
            ServiceAreaRollupStatus.DRAFT -> {
                when (next) {
                    ServiceAreaRollupStatus.ACTIVE, ServiceAreaRollupStatus.SUSPENDED -> return true
                }
                return false
            }
            ServiceAreaRollupStatus.PENDING -> return next == ServiceAreaRollupStatus.SUSPENDED || next == ServiceAreaRollupStatus.CLOSED
            ServiceAreaRollupStatus.ACTIVE -> return next == ServiceAreaRollupStatus.CLOSED
            ServiceAreaRollupStatus.SUSPENDED -> {
                when (next) {
                    ServiceAreaRollupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaRollupStatus) {
        if (!canMoveTo(next)) {
            error("service area rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): ServiceAreaRollup {
        return ServiceAreaRollup(
            code = code,
            label = label,
            title = title,
            graceDays = graceDays,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a service area rollup in status draft. */
fun newServiceAreaRollup(code: String, label: String): ServiceAreaRollup {
    return ServiceAreaRollup(
        code = code,
        label = label,
        title = "",
        graceDays = 0,
        currency = "",
        status = ServiceAreaRollupStatus.DRAFT,
    )
}
