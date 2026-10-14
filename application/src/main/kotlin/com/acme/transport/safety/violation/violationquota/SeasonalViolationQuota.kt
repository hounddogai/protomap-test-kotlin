package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal violation quota records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a seasonal violation quota is in its life cycle. */
enum class SeasonalViolationQuotaStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A seasonal violation quota. Its code is unique within a tenant. */
data class SeasonalViolationQuota(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var endsAt: Int,
    var floorValue: Double,
    var description: String,
    var timezone: String,
    var shiftCode: String,
    var status: SeasonalViolationQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalViolationQuotaStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalViolationQuotaStatus): Boolean {
        when (status) {
            SeasonalViolationQuotaStatus.CREATED -> {
                when (next) {
                    SeasonalViolationQuotaStatus.VALIDATED, SeasonalViolationQuotaStatus.PUBLISHED -> return true
                }
                return false
            }
            SeasonalViolationQuotaStatus.VALIDATED -> return next == SeasonalViolationQuotaStatus.PUBLISHED || next == SeasonalViolationQuotaStatus.RETIRED
            SeasonalViolationQuotaStatus.PUBLISHED -> return next == SeasonalViolationQuotaStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SeasonalViolationQuotaStatus) {
        if (!canMoveTo(next)) {
            error("seasonal violation quota $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round((endsAt.toDouble() - floorValue) * 100) / 100

    fun snapshot(): SeasonalViolationQuota {
        return SeasonalViolationQuota(
            code = code,
            label = label,
            attributes = attributes,
            endsAt = endsAt,
            floorValue = floorValue,
            description = description,
            timezone = timezone,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal violation quota in status created. */
fun newSeasonalViolationQuota(code: String, label: String): SeasonalViolationQuota {
    return SeasonalViolationQuota(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        endsAt = 0,
        floorValue = 0.0,
        description = "",
        timezone = "",
        shiftCode = "",
        status = SeasonalViolationQuotaStatus.CREATED,
    )
}
