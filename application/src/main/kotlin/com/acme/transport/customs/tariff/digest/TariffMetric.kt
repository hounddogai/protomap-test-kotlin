package com.acme.transport.customs.tariff.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff metric is in its life cycle. */
enum class TariffMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tariff metric and its current status. */
data class TariffMetric(
    var code: String,
    var name: String,
    var weight: Double,
    var updatedAt: Int,
    var description: String,
    var distanceKm: Double,
    var loadKg: Double,
    var odometer: Int,
    var active: Boolean,
    var carrier: String,
    var phone: String,
    var status: TariffMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffMetricStatus): Boolean {
        when (status) {
            TariffMetricStatus.NEW -> return next == TariffMetricStatus.RUNNING
            TariffMetricStatus.QUEUED -> {
                when (next) {
                    TariffMetricStatus.RUNNING -> return true
                }
                return false
            }
            TariffMetricStatus.RUNNING -> return next == TariffMetricStatus.FAILED
            TariffMetricStatus.DONE -> return next == TariffMetricStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TariffMetricStatus) {
        if (!canMoveTo(next)) {
            error("tariff metric $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedVolume(): Double = round(distanceKm * loadKg * 100) / 100

    fun snapshot(): TariffMetric {
        return TariffMetric(
            code = code,
            name = name,
            weight = weight,
            updatedAt = updatedAt,
            description = description,
            distanceKm = distanceKm,
            loadKg = loadKg,
            odometer = odometer,
            active = active,
            carrier = carrier,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tariff metric in status new. */
fun newTariffMetric(code: String, name: String): TariffMetric {
    return TariffMetric(
        code = code,
        name = name,
        weight = 0.0,
        updatedAt = 0,
        description = "",
        distanceKm = 0.0,
        loadKg = 0.0,
        odometer = 0,
        active = false,
        carrier = "",
        phone = "",
        status = TariffMetricStatus.NEW,
    )
}
