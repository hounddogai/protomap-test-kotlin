package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy duty period threshold records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a legacy duty period threshold is in its life cycle. */
enum class LegacyDutyPeriodThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One legacy duty period threshold, with what the planners need to act on it. */
data class LegacyDutyPeriodThreshold(
    var code: String,
    var label: String,
    var discount: Double,
    var routeCode: String,
    var timezone: String,
    var amount: Double,
    var minimum: Double,
    var endsAt: Int,
    var tags: MutableList<String>,
    var attributes: MutableMap<String, String>,
    var status: LegacyDutyPeriodThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDutyPeriodThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDutyPeriodThresholdStatus): Boolean {
        when (status) {
            LegacyDutyPeriodThresholdStatus.PLANNED -> return next == LegacyDutyPeriodThresholdStatus.COMPLETED
            LegacyDutyPeriodThresholdStatus.SCHEDULED -> {
                when (next) {
                    LegacyDutyPeriodThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyDutyPeriodThresholdStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyDutyPeriodThresholdStatus.COMPLETED, LegacyDutyPeriodThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyDutyPeriodThresholdStatus.COMPLETED -> {
                when (next) {
                    LegacyDutyPeriodThresholdStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyDutyPeriodThresholdStatus) {
        if (!canMoveTo(next)) {
            error("legacy duty period threshold $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round((amount + discount) * 100) / 100

    fun snapshot(): LegacyDutyPeriodThreshold {
        return LegacyDutyPeriodThreshold(
            code = code,
            label = label,
            discount = discount,
            routeCode = routeCode,
            timezone = timezone,
            amount = amount,
            minimum = minimum,
            endsAt = endsAt,
            tags = tags,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy duty period threshold in status planned. */
fun newLegacyDutyPeriodThreshold(code: String, label: String): LegacyDutyPeriodThreshold {
    return LegacyDutyPeriodThreshold(
        code = code,
        label = label,
        discount = 0.0,
        routeCode = "",
        timezone = "",
        amount = 0.0,
        minimum = 0.0,
        endsAt = 0,
        tags = mutableListOf<String>(),
        attributes = mutableMapOf<String, String>(),
        status = LegacyDutyPeriodThresholdStatus.PLANNED,
    )
}
