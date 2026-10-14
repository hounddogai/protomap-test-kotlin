package com.acme.transport.crewing.swap.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional swap entry records of crewing's swap work, and the rules, stores, and calculations around
// them.

/** Where a regional swap entry is in its life cycle. */
enum class RegionalSwapEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a regional swap entry and its current status. */
data class RegionalSwapEntry(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var amount: Double,
    var externalRef: String,
    var unit: String,
    var pinned: Boolean,
    var routeCode: String,
    var minimum: Double,
    var segment: String,
    var region: String,
    var status: RegionalSwapEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalSwapEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalSwapEntryStatus): Boolean {
        when (status) {
            RegionalSwapEntryStatus.DRAFT -> return next == RegionalSwapEntryStatus.PENDING || next == RegionalSwapEntryStatus.CLOSED
            RegionalSwapEntryStatus.PENDING -> return next == RegionalSwapEntryStatus.CLOSED
            RegionalSwapEntryStatus.ACTIVE -> {
                when (next) {
                    RegionalSwapEntryStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalSwapEntryStatus.SUSPENDED -> {
                when (next) {
                    RegionalSwapEntryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalSwapEntryStatus) {
        if (!canMoveTo(next)) {
            error("regional swap entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round((amount + minimum) * 100) / 100

    fun snapshot(): RegionalSwapEntry {
        return RegionalSwapEntry(
            code = code,
            title = title,
            tags = tags,
            amount = amount,
            externalRef = externalRef,
            unit = unit,
            pinned = pinned,
            routeCode = routeCode,
            minimum = minimum,
            segment = segment,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional swap entry in status draft. */
fun newRegionalSwapEntry(code: String, title: String): RegionalSwapEntry {
    return RegionalSwapEntry(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        amount = 0.0,
        externalRef = "",
        unit = "",
        pinned = false,
        routeCode = "",
        minimum = 0.0,
        segment = "",
        region = "",
        status = RegionalSwapEntryStatus.DRAFT,
    )
}
