package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute rollup is in its life cycle. */
enum class DisputeRollupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One dispute rollup, with what the planners need to act on it. */
data class DisputeRollup(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var capacity: Int,
    var vehicleId: String,
    var title: String,
    var quantity: Int,
    var region: String,
    var ceiling: Double,
    var dock: String,
    var externalRef: String,
    var status: DisputeRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeRollupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeRollupStatus): Boolean {
        when (status) {
            DisputeRollupStatus.DRAFT -> {
                when (next) {
                    DisputeRollupStatus.PENDING -> return true
                }
                return false
            }
            DisputeRollupStatus.PENDING -> return next == DisputeRollupStatus.CLOSED
            DisputeRollupStatus.ACTIVE -> return next == DisputeRollupStatus.SUSPENDED
            DisputeRollupStatus.SUSPENDED -> {
                when (next) {
                    DisputeRollupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeRollupStatus) {
        if (!canMoveTo(next)) {
            error("dispute rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedValue(): Double = round((capacity.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): DisputeRollup {
        return DisputeRollup(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            capacity = capacity,
            vehicleId = vehicleId,
            title = title,
            quantity = quantity,
            region = region,
            ceiling = ceiling,
            dock = dock,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a dispute rollup in status draft. */
fun newDisputeRollup(code: String, label: String): DisputeRollup {
    return DisputeRollup(
        code = code,
        label = label,
        fuelLiters = 0.0,
        capacity = 0,
        vehicleId = "",
        title = "",
        quantity = 0,
        region = "",
        ceiling = 0.0,
        dock = "",
        externalRef = "",
        status = DisputeRollupStatus.DRAFT,
    )
}
