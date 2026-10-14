package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation override is in its life cycle. */
enum class SortationOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A sortation override as the sortation team keeps it. */
data class SortationOverride(
    var code: String,
    var label: String,
    var currency: String,
    var carrier: String,
    var discount: Double,
    var margin: Double,
    var loadKg: Double,
    var reviewer: String,
    var timezone: String,
    var etaMinutes: Int,
    var rate: Double,
    var status: SortationOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationOverrideStatus): Boolean {
        when (status) {
            SortationOverrideStatus.CREATED -> {
                when (next) {
                    SortationOverrideStatus.VALIDATED, SortationOverrideStatus.RETIRED -> return true
                }
                return false
            }
            SortationOverrideStatus.VALIDATED -> {
                when (next) {
                    SortationOverrideStatus.PUBLISHED, SortationOverrideStatus.RETIRED -> return true
                }
                return false
            }
            SortationOverrideStatus.PUBLISHED -> return next == SortationOverrideStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SortationOverrideStatus) {
        if (!canMoveTo(next)) {
            error("sortation override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedLoad(): Double = round((margin - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): SortationOverride {
        return SortationOverride(
            code = code,
            label = label,
            currency = currency,
            carrier = carrier,
            discount = discount,
            margin = margin,
            loadKg = loadKg,
            reviewer = reviewer,
            timezone = timezone,
            etaMinutes = etaMinutes,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a sortation override in status created. */
fun newSortationOverride(code: String, label: String): SortationOverride {
    return SortationOverride(
        code = code,
        label = label,
        currency = "",
        carrier = "",
        discount = 0.0,
        margin = 0.0,
        loadKg = 0.0,
        reviewer = "",
        timezone = "",
        etaMinutes = 0,
        rate = 0.0,
        status = SortationOverrideStatus.CREATED,
    )
}
