package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional redelivery entry records of lastmile's redelivery work, and the rules, stores, and
// calculations around them.

/** Where a regional redelivery entry is in its life cycle. */
enum class RegionalRedeliveryEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional redelivery entry. Its code is unique within a tenant. */
data class RegionalRedeliveryEntry(
    var code: String,
    var label: String,
    var windowDays: Int,
    var checksum: String,
    var distanceKm: Double,
    var routeCode: String,
    var quantity: Int,
    var budget: Double,
    var attributes: MutableMap<String, String>,
    var status: RegionalRedeliveryEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRedeliveryEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRedeliveryEntryStatus): Boolean {
        when (status) {
            RegionalRedeliveryEntryStatus.CREATED -> {
                when (next) {
                    RegionalRedeliveryEntryStatus.RETIRED -> return true
                }
                return false
            }
            RegionalRedeliveryEntryStatus.VALIDATED -> return next == RegionalRedeliveryEntryStatus.PUBLISHED || next == RegionalRedeliveryEntryStatus.RETIRED
            RegionalRedeliveryEntryStatus.PUBLISHED -> return next == RegionalRedeliveryEntryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalRedeliveryEntryStatus) {
        if (!canMoveTo(next)) {
            error("regional redelivery entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional redelivery entry"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round((budget - distanceKm) * 100) / 100

    fun snapshot(): RegionalRedeliveryEntry {
        return RegionalRedeliveryEntry(
            code = code,
            label = label,
            windowDays = windowDays,
            checksum = checksum,
            distanceKm = distanceKm,
            routeCode = routeCode,
            quantity = quantity,
            budget = budget,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional redelivery entry in status created. */
fun newRegionalRedeliveryEntry(code: String, label: String): RegionalRedeliveryEntry {
    return RegionalRedeliveryEntry(
        code = code,
        label = label,
        windowDays = 0,
        checksum = "",
        distanceKm = 0.0,
        routeCode = "",
        quantity = 0,
        budget = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = RegionalRedeliveryEntryStatus.CREATED,
    )
}
