package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit forecast is in its life cycle. */
enum class AuditForecastStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a audit forecast and its current status. */
data class AuditForecast(
    var code: String,
    var label: String,
    var unit: String,
    var weight: Double,
    var notes: String,
    var region: String,
    var description: String,
    var endsAt: Int,
    var shiftCode: String,
    var priority: Int,
    var status: AuditForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditForecastStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditForecastStatus): Boolean {
        when (status) {
            AuditForecastStatus.NEW -> return next == AuditForecastStatus.QUEUED
            AuditForecastStatus.QUEUED -> return next == AuditForecastStatus.RUNNING || next == AuditForecastStatus.FAILED
            AuditForecastStatus.RUNNING -> {
                when (next) {
                    AuditForecastStatus.FAILED -> return true
                }
                return false
            }
            AuditForecastStatus.DONE -> return next == AuditForecastStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AuditForecastStatus) {
        if (!canMoveTo(next)) {
            error("audit forecast $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"audit forecast"} $code"

    fun netVolume(): Double = round((endsAt.toDouble() - weight) * 100) / 100

    fun snapshot(): AuditForecast {
        return AuditForecast(
            code = code,
            label = label,
            unit = unit,
            weight = weight,
            notes = notes,
            region = region,
            description = description,
            endsAt = endsAt,
            shiftCode = shiftCode,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a audit forecast in status new. */
fun newAuditForecast(code: String, label: String): AuditForecast {
    return AuditForecast(
        code = code,
        label = label,
        unit = "",
        weight = 0.0,
        notes = "",
        region = "",
        description = "",
        endsAt = 0,
        shiftCode = "",
        priority = 0,
        status = AuditForecastStatus.NEW,
    )
}
