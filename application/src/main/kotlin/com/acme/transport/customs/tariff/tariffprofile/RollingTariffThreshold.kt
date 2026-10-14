package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling tariff threshold is in its life cycle. */
enum class RollingTariffThresholdStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One rolling tariff threshold, with what the planners need to act on it. */
data class RollingTariffThreshold(
    var code: String,
    var label: String,
    var dock: String,
    var windowDays: Int,
    var contactEmail: String,
    var amount: Double,
    var vehicleId: String,
    var depot: String,
    var graceDays: Int,
    var expiresAt: Int,
    var status: RollingTariffThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTariffThresholdStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTariffThresholdStatus): Boolean {
        when (status) {
            RollingTariffThresholdStatus.CREATED -> return next == RollingTariffThresholdStatus.PUBLISHED
            RollingTariffThresholdStatus.VALIDATED -> return next == RollingTariffThresholdStatus.PUBLISHED
            RollingTariffThresholdStatus.PUBLISHED -> return next == RollingTariffThresholdStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingTariffThresholdStatus) {
        if (!canMoveTo(next)) {
            error("rolling tariff threshold $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"rolling tariff threshold"} $code"

    fun adjustedValue(): Double = round(expiresAt.toDouble() * amount * 100) / 100

    fun snapshot(): RollingTariffThreshold {
        return RollingTariffThreshold(
            code = code,
            label = label,
            dock = dock,
            windowDays = windowDays,
            contactEmail = contactEmail,
            amount = amount,
            vehicleId = vehicleId,
            depot = depot,
            graceDays = graceDays,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rolling tariff threshold in status created. */
fun newRollingTariffThreshold(code: String, label: String): RollingTariffThreshold {
    return RollingTariffThreshold(
        code = code,
        label = label,
        dock = "",
        windowDays = 0,
        contactEmail = "",
        amount = 0.0,
        vehicleId = "",
        depot = "",
        graceDays = 0,
        expiresAt = 0,
        status = RollingTariffThresholdStatus.CREATED,
    )
}
