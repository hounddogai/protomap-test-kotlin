package com.acme.transport.customs.tariff.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted tariff audit is in its life cycle. */
enum class WeightedTariffAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a weighted tariff audit and its current status. */
data class WeightedTariffAudit(
    var code: String,
    var label: String,
    var rate: Double,
    var timezone: String,
    var ceiling: Double,
    var approved: Boolean,
    var revision: Int,
    var status: WeightedTariffAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTariffAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTariffAuditStatus): Boolean {
        when (status) {
            WeightedTariffAuditStatus.PLANNED -> {
                when (next) {
                    WeightedTariffAuditStatus.IN_PROGRESS -> return true
                }
                return false
            }
            WeightedTariffAuditStatus.SCHEDULED -> return next == WeightedTariffAuditStatus.IN_PROGRESS
            WeightedTariffAuditStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedTariffAuditStatus.COMPLETED, WeightedTariffAuditStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedTariffAuditStatus.COMPLETED -> return next == WeightedTariffAuditStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedTariffAuditStatus) {
        if (!canMoveTo(next)) {
            error("weighted tariff audit $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"weighted tariff audit"} $code"

    fun adjustedExposure(): Double = round((rate + revision.toDouble()) * 100) / 100

    fun snapshot(): WeightedTariffAudit {
        return WeightedTariffAudit(
            code = code,
            label = label,
            rate = rate,
            timezone = timezone,
            ceiling = ceiling,
            approved = approved,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted tariff audit in status planned. */
fun newWeightedTariffAudit(code: String, label: String): WeightedTariffAudit {
    return WeightedTariffAudit(
        code = code,
        label = label,
        rate = 0.0,
        timezone = "",
        ceiling = 0.0,
        approved = false,
        revision = 0,
        status = WeightedTariffAuditStatus.PLANNED,
    )
}
