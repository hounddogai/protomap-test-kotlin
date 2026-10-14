package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled zone template is in its life cycle. */
enum class ScheduledZoneTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scheduled zone template as the zone team keeps it. */
data class ScheduledZoneTemplate(
    var code: String,
    var label: String,
    var windowDays: Int,
    var amount: Double,
    var checksum: String,
    var fuelLiters: Double,
    var attempts: Int,
    var source: String,
    var status: ScheduledZoneTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledZoneTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledZoneTemplateStatus): Boolean {
        when (status) {
            ScheduledZoneTemplateStatus.IDLE -> {
                when (next) {
                    ScheduledZoneTemplateStatus.WARMING, ScheduledZoneTemplateStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledZoneTemplateStatus.WARMING -> return next == ScheduledZoneTemplateStatus.STOPPED
            ScheduledZoneTemplateStatus.READY -> {
                when (next) {
                    ScheduledZoneTemplateStatus.DRAINING, ScheduledZoneTemplateStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledZoneTemplateStatus.DRAINING -> return next == ScheduledZoneTemplateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledZoneTemplateStatus) {
        if (!canMoveTo(next)) {
            error("scheduled zone template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netLoad(): Double = round((attempts.toDouble() - amount) * 100) / 100

    fun snapshot(): ScheduledZoneTemplate {
        return ScheduledZoneTemplate(
            code = code,
            label = label,
            windowDays = windowDays,
            amount = amount,
            checksum = checksum,
            fuelLiters = fuelLiters,
            attempts = attempts,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a scheduled zone template in status idle. */
fun newScheduledZoneTemplate(code: String, label: String): ScheduledZoneTemplate {
    return ScheduledZoneTemplate(
        code = code,
        label = label,
        windowDays = 0,
        amount = 0.0,
        checksum = "",
        fuelLiters = 0.0,
        attempts = 0,
        source = "",
        status = ScheduledZoneTemplateStatus.IDLE,
    )
}
