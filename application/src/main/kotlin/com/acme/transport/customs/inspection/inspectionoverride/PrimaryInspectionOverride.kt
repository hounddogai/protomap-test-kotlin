package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary inspection override records of customs's inspection work, and the rules, stores, and
// calculations around them.

/** Where a primary inspection override is in its life cycle. */
enum class PrimaryInspectionOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A primary inspection override as the inspection team keeps it. */
data class PrimaryInspectionOverride(
    var code: String,
    var name: String,
    var startsAt: Int,
    var channel: String,
    var pinned: Boolean,
    var fuelLiters: Double,
    var retries: Int,
    var contactEmail: String,
    var ratio: Double,
    var amount: Double,
    var ceiling: Double,
    var status: PrimaryInspectionOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryInspectionOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryInspectionOverrideStatus): Boolean {
        when (status) {
            PrimaryInspectionOverrideStatus.OPEN -> {
                when (next) {
                    PrimaryInspectionOverrideStatus.IN_REVIEW, PrimaryInspectionOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryInspectionOverrideStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryInspectionOverrideStatus.RESOLVED, PrimaryInspectionOverrideStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryInspectionOverrideStatus.RESOLVED -> {
                when (next) {
                    PrimaryInspectionOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryInspectionOverrideStatus.REOPENED -> return next == PrimaryInspectionOverrideStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryInspectionOverrideStatus) {
        if (!canMoveTo(next)) {
            error("primary inspection override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun weightedLoad(): Double = round(startsAt.toDouble() * amount * 100) / 100

    fun snapshot(): PrimaryInspectionOverride {
        return PrimaryInspectionOverride(
            code = code,
            name = name,
            startsAt = startsAt,
            channel = channel,
            pinned = pinned,
            fuelLiters = fuelLiters,
            retries = retries,
            contactEmail = contactEmail,
            ratio = ratio,
            amount = amount,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a primary inspection override in status open. */
fun newPrimaryInspectionOverride(code: String, name: String): PrimaryInspectionOverride {
    return PrimaryInspectionOverride(
        code = code,
        name = name,
        startsAt = 0,
        channel = "",
        pinned = false,
        fuelLiters = 0.0,
        retries = 0,
        contactEmail = "",
        ratio = 0.0,
        amount = 0.0,
        ceiling = 0.0,
        status = PrimaryInspectionOverrideStatus.OPEN,
    )
}
