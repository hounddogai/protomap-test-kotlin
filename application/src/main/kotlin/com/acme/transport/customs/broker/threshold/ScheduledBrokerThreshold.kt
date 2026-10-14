package com.acme.transport.customs.broker.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled broker threshold records of customs's broker work, and the rules, stores, and calculations
// around them.

/** Where a scheduled broker threshold is in its life cycle. */
enum class ScheduledBrokerThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scheduled broker threshold. Its code is unique within a tenant. */
data class ScheduledBrokerThreshold(
    var code: String,
    var title: String,
    var loadKg: Double,
    var name: String,
    var graceDays: Int,
    var currency: String,
    var status: ScheduledBrokerThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledBrokerThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledBrokerThresholdStatus): Boolean {
        when (status) {
            ScheduledBrokerThresholdStatus.PLANNED -> {
                when (next) {
                    ScheduledBrokerThresholdStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ScheduledBrokerThresholdStatus.SCHEDULED -> return next == ScheduledBrokerThresholdStatus.IN_PROGRESS || next == ScheduledBrokerThresholdStatus.COMPLETED
            ScheduledBrokerThresholdStatus.IN_PROGRESS -> return next == ScheduledBrokerThresholdStatus.COMPLETED
            ScheduledBrokerThresholdStatus.COMPLETED -> {
                when (next) {
                    ScheduledBrokerThresholdStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledBrokerThresholdStatus) {
        if (!canMoveTo(next)) {
            error("scheduled broker threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netLoad(): Double = round((loadKg - graceDays.toDouble()) * 100) / 100

    fun snapshot(): ScheduledBrokerThreshold {
        return ScheduledBrokerThreshold(
            code = code,
            title = title,
            loadKg = loadKg,
            name = name,
            graceDays = graceDays,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled broker threshold in status planned. */
fun newScheduledBrokerThreshold(code: String, title: String): ScheduledBrokerThreshold {
    return ScheduledBrokerThreshold(
        code = code,
        title = title,
        loadKg = 0.0,
        name = "",
        graceDays = 0,
        currency = "",
        status = ScheduledBrokerThresholdStatus.PLANNED,
    )
}
