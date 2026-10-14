package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional qualification override is in its life cycle. */
enum class RegionalQualificationOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One regional qualification override, with what the planners need to act on it. */
data class RegionalQualificationOverride(
    var code: String,
    var label: String,
    var active: Boolean,
    var budget: Double,
    var pinned: Boolean,
    var cost: Double,
    var status: RegionalQualificationOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalQualificationOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalQualificationOverrideStatus): Boolean {
        when (status) {
            RegionalQualificationOverrideStatus.OPEN -> {
                when (next) {
                    RegionalQualificationOverrideStatus.RESOLVED, RegionalQualificationOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalQualificationOverrideStatus.IN_REVIEW -> return next == RegionalQualificationOverrideStatus.RESOLVED || next == RegionalQualificationOverrideStatus.REOPENED
            RegionalQualificationOverrideStatus.RESOLVED -> {
                when (next) {
                    RegionalQualificationOverrideStatus.REOPENED -> return true
                }
                return false
            }
            RegionalQualificationOverrideStatus.REOPENED -> {
                when (next) {
                    RegionalQualificationOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalQualificationOverrideStatus) {
        if (!canMoveTo(next)) {
            error("regional qualification override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedLoad(): Double = round(cost * budget * 100) / 100

    fun snapshot(): RegionalQualificationOverride {
        return RegionalQualificationOverride(
            code = code,
            label = label,
            active = active,
            budget = budget,
            pinned = pinned,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional qualification override in status open. */
fun newRegionalQualificationOverride(code: String, label: String): RegionalQualificationOverride {
    return RegionalQualificationOverride(
        code = code,
        label = label,
        active = false,
        budget = 0.0,
        pinned = false,
        cost = 0.0,
        status = RegionalQualificationOverrideStatus.OPEN,
    )
}
