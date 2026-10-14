package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The duty period threshold records of crewing's duty period work, and the rules, stores, and calculations
// around them.

/** Where a duty period threshold is in its life cycle. */
enum class DutyPeriodThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A duty period threshold. Its code is unique within a tenant. */
data class DutyPeriodThreshold(
    var code: String,
    var label: String,
    var budget: Double,
    var version: Int,
    var title: String,
    var etaMinutes: Int,
    var dock: String,
    var locale: String,
    var source: String,
    var status: DutyPeriodThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodThresholdStatus): Boolean {
        when (status) {
            DutyPeriodThresholdStatus.REQUESTED -> {
                when (next) {
                    DutyPeriodThresholdStatus.REVOKED -> return true
                }
                return false
            }
            DutyPeriodThresholdStatus.GRANTED -> {
                when (next) {
                    DutyPeriodThresholdStatus.REVOKED, DutyPeriodThresholdStatus.EXPIRED -> return true
                }
                return false
            }
            DutyPeriodThresholdStatus.REVOKED -> return next == DutyPeriodThresholdStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DutyPeriodThresholdStatus) {
        if (!canMoveTo(next)) {
            error("duty period threshold $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveLoad(): Double = round((budget + version.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodThreshold {
        return DutyPeriodThreshold(
            code = code,
            label = label,
            budget = budget,
            version = version,
            title = title,
            etaMinutes = etaMinutes,
            dock = dock,
            locale = locale,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a duty period threshold in status requested. */
fun newDutyPeriodThreshold(code: String, label: String): DutyPeriodThreshold {
    return DutyPeriodThreshold(
        code = code,
        label = label,
        budget = 0.0,
        version = 0,
        title = "",
        etaMinutes = 0,
        dock = "",
        locale = "",
        source = "",
        status = DutyPeriodThresholdStatus.REQUESTED,
    )
}
