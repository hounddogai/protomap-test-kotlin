package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional dwell profile is in its life cycle. */
enum class RegionalDwellProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A regional dwell profile as the dwell team keeps it. */
data class RegionalDwellProfile(
    var code: String,
    var title: String,
    var approved: Boolean,
    var discount: Double,
    var maximum: Double,
    var segment: String,
    var graceDays: Int,
    var status: RegionalDwellProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDwellProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDwellProfileStatus): Boolean {
        when (status) {
            RegionalDwellProfileStatus.OPEN -> {
                when (next) {
                    RegionalDwellProfileStatus.REOPENED, RegionalDwellProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalDwellProfileStatus.IN_REVIEW -> {
                when (next) {
                    RegionalDwellProfileStatus.REOPENED, RegionalDwellProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalDwellProfileStatus.RESOLVED -> return next == RegionalDwellProfileStatus.REOPENED
            RegionalDwellProfileStatus.REOPENED -> return next == RegionalDwellProfileStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RegionalDwellProfileStatus) {
        if (!canMoveTo(next)) {
            error("regional dwell profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional dwell profile"} $code"

    fun effectiveValue(): Double = round((maximum + discount) * 100) / 100

    fun snapshot(): RegionalDwellProfile {
        return RegionalDwellProfile(
            code = code,
            title = title,
            approved = approved,
            discount = discount,
            maximum = maximum,
            segment = segment,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a regional dwell profile in status open. */
fun newRegionalDwellProfile(code: String, title: String): RegionalDwellProfile {
    return RegionalDwellProfile(
        code = code,
        title = title,
        approved = false,
        discount = 0.0,
        maximum = 0.0,
        segment = "",
        graceDays = 0,
        status = RegionalDwellProfileStatus.OPEN,
    )
}
