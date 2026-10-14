package com.acme.transport.customs.tariff.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual tariff schedule records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a manual tariff schedule is in its life cycle. */
enum class ManualTariffScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A manual tariff schedule as the tariff team keeps it. */
data class ManualTariffSchedule(
    var code: String,
    var name: String,
    var lane: String,
    var etaMinutes: Int,
    var ratio: Double,
    var priority: Int,
    var visible: Boolean,
    var approved: Boolean,
    var pallets: Int,
    var ownerId: String,
    var revision: Int,
    var status: ManualTariffScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTariffScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTariffScheduleStatus): Boolean {
        when (status) {
            ManualTariffScheduleStatus.OPEN -> {
                when (next) {
                    ManualTariffScheduleStatus.REOPENED -> return true
                }
                return false
            }
            ManualTariffScheduleStatus.IN_REVIEW -> {
                when (next) {
                    ManualTariffScheduleStatus.RESOLVED -> return true
                }
                return false
            }
            ManualTariffScheduleStatus.RESOLVED -> {
                when (next) {
                    ManualTariffScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualTariffScheduleStatus.REOPENED -> return next == ManualTariffScheduleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ManualTariffScheduleStatus) {
        if (!canMoveTo(next)) {
            error("manual tariff schedule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun projectedExposure(): Double = round(etaMinutes.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): ManualTariffSchedule {
        return ManualTariffSchedule(
            code = code,
            name = name,
            lane = lane,
            etaMinutes = etaMinutes,
            ratio = ratio,
            priority = priority,
            visible = visible,
            approved = approved,
            pallets = pallets,
            ownerId = ownerId,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual tariff schedule in status open. */
fun newManualTariffSchedule(code: String, name: String): ManualTariffSchedule {
    return ManualTariffSchedule(
        code = code,
        name = name,
        lane = "",
        etaMinutes = 0,
        ratio = 0.0,
        priority = 0,
        visible = false,
        approved = false,
        pallets = 0,
        ownerId = "",
        revision = 0,
        status = ManualTariffScheduleStatus.OPEN,
    )
}
