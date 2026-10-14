package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.text.normalize

/** Where a regional sortation override is in its life cycle. */
enum class RegionalSortationOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional sortation override and its current status. */
data class RegionalSortationOverride(
    var code: String,
    var name: String,
    var externalRef: String,
    var contactEmail: String,
    var windowDays: Int,
    var status: RegionalSortationOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalSortationOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalSortationOverrideStatus): Boolean {
        when (status) {
            RegionalSortationOverrideStatus.CREATED -> return next == RegionalSortationOverrideStatus.PUBLISHED
            RegionalSortationOverrideStatus.VALIDATED -> return next == RegionalSortationOverrideStatus.PUBLISHED
            RegionalSortationOverrideStatus.PUBLISHED -> {
                when (next) {
                    RegionalSortationOverrideStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalSortationOverrideStatus) {
        if (!canMoveTo(next)) {
            error("regional sortation override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional sortation override"} $code"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): RegionalSortationOverride {
        return RegionalSortationOverride(
            code = code,
            name = name,
            externalRef = externalRef,
            contactEmail = contactEmail,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional sortation override in status created. */
fun newRegionalSortationOverride(code: String, name: String): RegionalSortationOverride {
    return RegionalSortationOverride(
        code = code,
        name = name,
        externalRef = "",
        contactEmail = "",
        windowDays = 0,
        status = RegionalSortationOverrideStatus.CREATED,
    )
}
