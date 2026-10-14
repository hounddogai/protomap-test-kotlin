package com.acme.transport.customs.broker.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker window is in its life cycle. */
enum class BrokerWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a broker window and its current status. */
data class BrokerWindow(
    var code: String,
    var label: String,
    var budget: Double,
    var approved: Boolean,
    var shiftCode: String,
    var title: String,
    var odometer: Int,
    var status: BrokerWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerWindowStatus): Boolean {
        when (status) {
            BrokerWindowStatus.CREATED -> {
                when (next) {
                    BrokerWindowStatus.VALIDATED, BrokerWindowStatus.PUBLISHED -> return true
                }
                return false
            }
            BrokerWindowStatus.VALIDATED -> {
                when (next) {
                    BrokerWindowStatus.RETIRED -> return true
                }
                return false
            }
            BrokerWindowStatus.PUBLISHED -> return next == BrokerWindowStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BrokerWindowStatus) {
        if (!canMoveTo(next)) {
            error("broker window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"broker window"} $code"

    fun projectedExposure(): Double = round(odometer.toDouble() * budget * 100) / 100

    fun snapshot(): BrokerWindow {
        return BrokerWindow(
            code = code,
            label = label,
            budget = budget,
            approved = approved,
            shiftCode = shiftCode,
            title = title,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a broker window in status created. */
fun newBrokerWindow(code: String, label: String): BrokerWindow {
    return BrokerWindow(
        code = code,
        label = label,
        budget = 0.0,
        approved = false,
        shiftCode = "",
        title = "",
        odometer = 0,
        status = BrokerWindowStatus.CREATED,
    )
}
