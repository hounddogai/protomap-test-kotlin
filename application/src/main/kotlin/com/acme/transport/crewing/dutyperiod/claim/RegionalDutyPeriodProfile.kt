package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional duty period profile is in its life cycle. */
enum class RegionalDutyPeriodProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional duty period profile as the duty period team keeps it. */
data class RegionalDutyPeriodProfile(
    var code: String,
    var name: String,
    var loadKg: Double,
    var weight: Double,
    var label: String,
    var externalRef: String,
    var reviewer: String,
    var attributes: MutableMap<String, String>,
    var stopCount: Int,
    var channel: String,
    var threshold: Double,
    var status: RegionalDutyPeriodProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDutyPeriodProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDutyPeriodProfileStatus): Boolean {
        when (status) {
            RegionalDutyPeriodProfileStatus.DRAFT -> return next == RegionalDutyPeriodProfileStatus.ACTIVE
            RegionalDutyPeriodProfileStatus.PENDING -> return next == RegionalDutyPeriodProfileStatus.SUSPENDED || next == RegionalDutyPeriodProfileStatus.CLOSED
            RegionalDutyPeriodProfileStatus.ACTIVE -> {
                when (next) {
                    RegionalDutyPeriodProfileStatus.SUSPENDED, RegionalDutyPeriodProfileStatus.CLOSED -> return true
                }
                return false
            }
            RegionalDutyPeriodProfileStatus.SUSPENDED -> return next == RegionalDutyPeriodProfileStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalDutyPeriodProfileStatus) {
        if (!canMoveTo(next)) {
            error("regional duty period profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional duty period profile"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round((loadKg - weight) * 100) / 100

    fun snapshot(): RegionalDutyPeriodProfile {
        return RegionalDutyPeriodProfile(
            code = code,
            name = name,
            loadKg = loadKg,
            weight = weight,
            label = label,
            externalRef = externalRef,
            reviewer = reviewer,
            attributes = attributes,
            stopCount = stopCount,
            channel = channel,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional duty period profile in status draft. */
fun newRegionalDutyPeriodProfile(code: String, name: String): RegionalDutyPeriodProfile {
    return RegionalDutyPeriodProfile(
        code = code,
        name = name,
        loadKg = 0.0,
        weight = 0.0,
        label = "",
        externalRef = "",
        reviewer = "",
        attributes = mutableMapOf<String, String>(),
        stopCount = 0,
        channel = "",
        threshold = 0.0,
        status = RegionalDutyPeriodProfileStatus.DRAFT,
    )
}
