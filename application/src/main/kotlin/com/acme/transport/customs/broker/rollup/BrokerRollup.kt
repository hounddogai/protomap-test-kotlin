package com.acme.transport.customs.broker.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The broker rollup records of customs's broker work, and the rules, stores, and calculations around them.

/** Where a broker rollup is in its life cycle. */
enum class BrokerRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A broker rollup. Its code is unique within a tenant. */
data class BrokerRollup(
    var code: String,
    var name: String,
    var unit: String,
    var depot: String,
    var threshold: Double,
    var label: String,
    var ratio: Double,
    var weight: Double,
    var margin: Double,
    var status: BrokerRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerRollupStatus): Boolean {
        when (status) {
            BrokerRollupStatus.PLANNED -> {
                when (next) {
                    BrokerRollupStatus.SCHEDULED -> return true
                }
                return false
            }
            BrokerRollupStatus.SCHEDULED -> {
                when (next) {
                    BrokerRollupStatus.IN_PROGRESS, BrokerRollupStatus.CANCELLED -> return true
                }
                return false
            }
            BrokerRollupStatus.IN_PROGRESS -> {
                when (next) {
                    BrokerRollupStatus.CANCELLED -> return true
                }
                return false
            }
            BrokerRollupStatus.COMPLETED -> return next == BrokerRollupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BrokerRollupStatus) {
        if (!canMoveTo(next)) {
            error("broker rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"broker rollup"} $code"

    fun effectiveLoad(): Double = round((threshold - ratio) * 100) / 100

    fun snapshot(): BrokerRollup {
        return BrokerRollup(
            code = code,
            name = name,
            unit = unit,
            depot = depot,
            threshold = threshold,
            label = label,
            ratio = ratio,
            weight = weight,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a broker rollup in status planned. */
fun newBrokerRollup(code: String, name: String): BrokerRollup {
    return BrokerRollup(
        code = code,
        name = name,
        unit = "",
        depot = "",
        threshold = 0.0,
        label = "",
        ratio = 0.0,
        weight = 0.0,
        margin = 0.0,
        status = BrokerRollupStatus.PLANNED,
    )
}
