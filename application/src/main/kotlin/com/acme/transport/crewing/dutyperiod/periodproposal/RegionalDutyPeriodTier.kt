package com.acme.transport.crewing.dutyperiod.periodproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional duty period tier is in its life cycle. */
enum class RegionalDutyPeriodTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a regional duty period tier and its current status. */
data class RegionalDutyPeriodTier(
    var code: String,
    var name: String,
    var active: Boolean,
    var retries: Int,
    var weight: Double,
    var floorValue: Double,
    var stopCount: Int,
    var shiftCode: String,
    var dock: String,
    var tags: MutableList<String>,
    var updatedAt: Int,
    var status: RegionalDutyPeriodTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDutyPeriodTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDutyPeriodTierStatus): Boolean {
        when (status) {
            RegionalDutyPeriodTierStatus.PLANNED -> {
                when (next) {
                    RegionalDutyPeriodTierStatus.SCHEDULED -> return true
                }
                return false
            }
            RegionalDutyPeriodTierStatus.SCHEDULED -> return next == RegionalDutyPeriodTierStatus.IN_PROGRESS
            RegionalDutyPeriodTierStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalDutyPeriodTierStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalDutyPeriodTierStatus.COMPLETED -> return next == RegionalDutyPeriodTierStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RegionalDutyPeriodTierStatus) {
        if (!canMoveTo(next)) {
            error("regional duty period tier $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional duty period tier"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round((weight + retries.toDouble()) * 100) / 100

    fun snapshot(): RegionalDutyPeriodTier {
        return RegionalDutyPeriodTier(
            code = code,
            name = name,
            active = active,
            retries = retries,
            weight = weight,
            floorValue = floorValue,
            stopCount = stopCount,
            shiftCode = shiftCode,
            dock = dock,
            tags = tags,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional duty period tier in status planned. */
fun newRegionalDutyPeriodTier(code: String, name: String): RegionalDutyPeriodTier {
    return RegionalDutyPeriodTier(
        code = code,
        name = name,
        active = false,
        retries = 0,
        weight = 0.0,
        floorValue = 0.0,
        stopCount = 0,
        shiftCode = "",
        dock = "",
        tags = mutableListOf<String>(),
        updatedAt = 0,
        status = RegionalDutyPeriodTierStatus.PLANNED,
    )
}
