package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation cycle is in its life cycle. */
enum class SortationCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a sortation cycle and its current status. */
data class SortationCycle(
    var code: String,
    var title: String,
    var label: String,
    var discount: Double,
    var attributes: MutableMap<String, String>,
    var score: Double,
    var odometer: Int,
    var carrier: String,
    var status: SortationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationCycleStatus): Boolean {
        when (status) {
            SortationCycleStatus.REQUESTED -> return next == SortationCycleStatus.GRANTED || next == SortationCycleStatus.EXPIRED
            SortationCycleStatus.GRANTED -> return next == SortationCycleStatus.REVOKED || next == SortationCycleStatus.EXPIRED
            SortationCycleStatus.REVOKED -> return next == SortationCycleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SortationCycleStatus) {
        if (!canMoveTo(next)) {
            error("sortation cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"sortation cycle"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((discount - odometer.toDouble()) * 100) / 100

    fun snapshot(): SortationCycle {
        return SortationCycle(
            code = code,
            title = title,
            label = label,
            discount = discount,
            attributes = attributes,
            score = score,
            odometer = odometer,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a sortation cycle in status requested. */
fun newSortationCycle(code: String, title: String): SortationCycle {
    return SortationCycle(
        code = code,
        title = title,
        label = "",
        discount = 0.0,
        attributes = mutableMapOf<String, String>(),
        score = 0.0,
        odometer = 0,
        carrier = "",
        status = SortationCycleStatus.REQUESTED,
    )
}
