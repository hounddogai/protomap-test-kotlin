package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The sortation tier records of terminal's sortation work, and the rules, stores, and calculations around
// them.

/** Where a sortation tier is in its life cycle. */
enum class SortationTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A sortation tier. Its code is unique within a tenant. */
data class SortationTier(
    var code: String,
    var label: String,
    var graceDays: Int,
    var odometer: Int,
    var attributes: MutableMap<String, String>,
    var budget: Double,
    var startsAt: Int,
    var durationMinutes: Int,
    var discount: Double,
    var ceiling: Double,
    var status: SortationTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationTierStatus): Boolean {
        when (status) {
            SortationTierStatus.PROPOSED -> {
                when (next) {
                    SortationTierStatus.APPROVED -> return true
                }
                return false
            }
            SortationTierStatus.APPROVED -> return next == SortationTierStatus.REJECTED || next == SortationTierStatus.WITHDRAWN
            SortationTierStatus.REJECTED -> return next == SortationTierStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: SortationTierStatus) {
        if (!canMoveTo(next)) {
            error("sortation tier $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"sortation tier"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round(startsAt.toDouble() * discount * 100) / 100

    fun snapshot(): SortationTier {
        return SortationTier(
            code = code,
            label = label,
            graceDays = graceDays,
            odometer = odometer,
            attributes = attributes,
            budget = budget,
            startsAt = startsAt,
            durationMinutes = durationMinutes,
            discount = discount,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a sortation tier in status proposed. */
fun newSortationTier(code: String, label: String): SortationTier {
    return SortationTier(
        code = code,
        label = label,
        graceDays = 0,
        odometer = 0,
        attributes = mutableMapOf<String, String>(),
        budget = 0.0,
        startsAt = 0,
        durationMinutes = 0,
        discount = 0.0,
        ceiling = 0.0,
        status = SortationTierStatus.PROPOSED,
    )
}
