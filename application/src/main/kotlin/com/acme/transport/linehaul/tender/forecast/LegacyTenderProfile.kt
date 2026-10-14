package com.acme.transport.linehaul.tender.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy tender profile is in its life cycle. */
enum class LegacyTenderProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy tender profile as the tender team keeps it. */
data class LegacyTenderProfile(
    var code: String,
    var label: String,
    var attempts: Int,
    var region: String,
    var summary: String,
    var pallets: Int,
    var graceDays: Int,
    var description: String,
    var title: String,
    var capacity: Int,
    var status: LegacyTenderProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTenderProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTenderProfileStatus): Boolean {
        when (status) {
            LegacyTenderProfileStatus.PLANNED -> {
                when (next) {
                    LegacyTenderProfileStatus.SCHEDULED -> return true
                }
                return false
            }
            LegacyTenderProfileStatus.SCHEDULED -> {
                when (next) {
                    LegacyTenderProfileStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LegacyTenderProfileStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyTenderProfileStatus.COMPLETED, LegacyTenderProfileStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyTenderProfileStatus.COMPLETED -> return next == LegacyTenderProfileStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyTenderProfileStatus) {
        if (!canMoveTo(next)) {
            error("legacy tender profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy tender profile"} $code"

    fun weightedTotal(): Double = round((capacity.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): LegacyTenderProfile {
        return LegacyTenderProfile(
            code = code,
            label = label,
            attempts = attempts,
            region = region,
            summary = summary,
            pallets = pallets,
            graceDays = graceDays,
            description = description,
            title = title,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a legacy tender profile in status planned. */
fun newLegacyTenderProfile(code: String, label: String): LegacyTenderProfile {
    return LegacyTenderProfile(
        code = code,
        label = label,
        attempts = 0,
        region = "",
        summary = "",
        pallets = 0,
        graceDays = 0,
        description = "",
        title = "",
        capacity = 0,
        status = LegacyTenderProfileStatus.PLANNED,
    )
}
