package com.acme.transport.customs.tariff.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default tariff request is in its life cycle. */
enum class DefaultTariffRequestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a default tariff request and its current status. */
data class DefaultTariffRequest(
    var code: String,
    var name: String,
    var timezone: String,
    var loadKg: Double,
    var retries: Int,
    var weight: Double,
    var endsAt: Int,
    var batchSize: Int,
    var startsAt: Int,
    var label: String,
    var lane: String,
    var status: DefaultTariffRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTariffRequestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTariffRequestStatus): Boolean {
        when (status) {
            DefaultTariffRequestStatus.PLANNED -> return next == DefaultTariffRequestStatus.SCHEDULED || next == DefaultTariffRequestStatus.IN_PROGRESS
            DefaultTariffRequestStatus.SCHEDULED -> {
                when (next) {
                    DefaultTariffRequestStatus.IN_PROGRESS, DefaultTariffRequestStatus.COMPLETED -> return true
                }
                return false
            }
            DefaultTariffRequestStatus.IN_PROGRESS -> return next == DefaultTariffRequestStatus.COMPLETED || next == DefaultTariffRequestStatus.CANCELLED
            DefaultTariffRequestStatus.COMPLETED -> {
                when (next) {
                    DefaultTariffRequestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultTariffRequestStatus) {
        if (!canMoveTo(next)) {
            error("default tariff request $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"default tariff request"} $code"

    fun adjustedExposure(): Double = round(retries.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): DefaultTariffRequest {
        return DefaultTariffRequest(
            code = code,
            name = name,
            timezone = timezone,
            loadKg = loadKg,
            retries = retries,
            weight = weight,
            endsAt = endsAt,
            batchSize = batchSize,
            startsAt = startsAt,
            label = label,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default tariff request in status planned. */
fun newDefaultTariffRequest(code: String, name: String): DefaultTariffRequest {
    return DefaultTariffRequest(
        code = code,
        name = name,
        timezone = "",
        loadKg = 0.0,
        retries = 0,
        weight = 0.0,
        endsAt = 0,
        batchSize = 0,
        startsAt = 0,
        label = "",
        lane = "",
        status = DefaultTariffRequestStatus.PLANNED,
    )
}
