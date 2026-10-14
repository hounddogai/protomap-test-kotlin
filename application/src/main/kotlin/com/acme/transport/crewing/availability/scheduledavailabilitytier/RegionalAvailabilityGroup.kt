package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional availability group is in its life cycle. */
enum class RegionalAvailabilityGroupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One regional availability group, with what the planners need to act on it. */
data class RegionalAvailabilityGroup(
    var code: String,
    var title: String,
    var windowDays: Int,
    var ratio: Double,
    var loadKg: Double,
    var pallets: Int,
    var timezone: String,
    var shiftCode: String,
    var status: RegionalAvailabilityGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalAvailabilityGroupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalAvailabilityGroupStatus): Boolean {
        when (status) {
            RegionalAvailabilityGroupStatus.CREATED -> return next == RegionalAvailabilityGroupStatus.PUBLISHED
            RegionalAvailabilityGroupStatus.VALIDATED -> return next == RegionalAvailabilityGroupStatus.PUBLISHED
            RegionalAvailabilityGroupStatus.PUBLISHED -> return next == RegionalAvailabilityGroupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalAvailabilityGroupStatus) {
        if (!canMoveTo(next)) {
            error("regional availability group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedValue(): Double = round(pallets.toDouble() * ratio * 100) / 100

    fun snapshot(): RegionalAvailabilityGroup {
        return RegionalAvailabilityGroup(
            code = code,
            title = title,
            windowDays = windowDays,
            ratio = ratio,
            loadKg = loadKg,
            pallets = pallets,
            timezone = timezone,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional availability group in status created. */
fun newRegionalAvailabilityGroup(code: String, title: String): RegionalAvailabilityGroup {
    return RegionalAvailabilityGroup(
        code = code,
        title = title,
        windowDays = 0,
        ratio = 0.0,
        loadKg = 0.0,
        pallets = 0,
        timezone = "",
        shiftCode = "",
        status = RegionalAvailabilityGroupStatus.CREATED,
    )
}
