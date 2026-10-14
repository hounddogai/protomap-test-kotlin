package com.acme.transport.linehaul.tender.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy tender template is in its life cycle. */
enum class LegacyTenderTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy tender template as the tender team keeps it. */
data class LegacyTenderTemplate(
    var code: String,
    var label: String,
    var rate: Double,
    var budget: Double,
    var currency: String,
    var driverId: String,
    var odometer: Int,
    var margin: Double,
    var segment: String,
    var durationMinutes: Int,
    var status: LegacyTenderTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTenderTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTenderTemplateStatus): Boolean {
        when (status) {
            LegacyTenderTemplateStatus.NEW -> {
                when (next) {
                    LegacyTenderTemplateStatus.QUEUED, LegacyTenderTemplateStatus.FAILED -> return true
                }
                return false
            }
            LegacyTenderTemplateStatus.QUEUED -> {
                when (next) {
                    LegacyTenderTemplateStatus.RUNNING -> return true
                }
                return false
            }
            LegacyTenderTemplateStatus.RUNNING -> return next == LegacyTenderTemplateStatus.DONE || next == LegacyTenderTemplateStatus.FAILED
            LegacyTenderTemplateStatus.DONE -> {
                when (next) {
                    LegacyTenderTemplateStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyTenderTemplateStatus) {
        if (!canMoveTo(next)) {
            error("legacy tender template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy tender template"} $code"

    fun adjustedLoad(): Double = round(rate * budget * 100) / 100

    fun snapshot(): LegacyTenderTemplate {
        return LegacyTenderTemplate(
            code = code,
            label = label,
            rate = rate,
            budget = budget,
            currency = currency,
            driverId = driverId,
            odometer = odometer,
            margin = margin,
            segment = segment,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a legacy tender template in status new. */
fun newLegacyTenderTemplate(code: String, label: String): LegacyTenderTemplate {
    return LegacyTenderTemplate(
        code = code,
        label = label,
        rate = 0.0,
        budget = 0.0,
        currency = "",
        driverId = "",
        odometer = 0,
        margin = 0.0,
        segment = "",
        durationMinutes = 0,
        status = LegacyTenderTemplateStatus.NEW,
    )
}
