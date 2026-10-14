package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending camera event forecast is in its life cycle. */
enum class PendingCameraEventForecastStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending camera event forecast. Its code is unique within a tenant. */
data class PendingCameraEventForecast(
    var code: String,
    var title: String,
    var contactEmail: String,
    var attributes: MutableMap<String, String>,
    var loadKg: Double,
    var graceDays: Int,
    var unit: String,
    var depot: String,
    var summary: String,
    var batchSize: Int,
    var version: Int,
    var status: PendingCameraEventForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCameraEventForecastStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCameraEventForecastStatus): Boolean {
        when (status) {
            PendingCameraEventForecastStatus.REQUESTED -> {
                when (next) {
                    PendingCameraEventForecastStatus.REVOKED, PendingCameraEventForecastStatus.EXPIRED -> return true
                }
                return false
            }
            PendingCameraEventForecastStatus.GRANTED -> return next == PendingCameraEventForecastStatus.REVOKED
            PendingCameraEventForecastStatus.REVOKED -> return next == PendingCameraEventForecastStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingCameraEventForecastStatus) {
        if (!canMoveTo(next)) {
            error("pending camera event forecast $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"pending camera event forecast"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round((loadKg + graceDays.toDouble()) * 100) / 100

    fun snapshot(): PendingCameraEventForecast {
        return PendingCameraEventForecast(
            code = code,
            title = title,
            contactEmail = contactEmail,
            attributes = attributes,
            loadKg = loadKg,
            graceDays = graceDays,
            unit = unit,
            depot = depot,
            summary = summary,
            batchSize = batchSize,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending camera event forecast in status requested. */
fun newPendingCameraEventForecast(code: String, title: String): PendingCameraEventForecast {
    return PendingCameraEventForecast(
        code = code,
        title = title,
        contactEmail = "",
        attributes = mutableMapOf<String, String>(),
        loadKg = 0.0,
        graceDays = 0,
        unit = "",
        depot = "",
        summary = "",
        batchSize = 0,
        version = 0,
        status = PendingCameraEventForecastStatus.REQUESTED,
    )
}
