package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.text.normalize

// The adaptive dwell group records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a adaptive dwell group is in its life cycle. */
enum class AdaptiveDwellGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a adaptive dwell group and its current status. */
data class AdaptiveDwellGroup(
    var code: String,
    var name: String,
    var graceDays: Int,
    var category: String,
    var dock: String,
    var label: String,
    var status: AdaptiveDwellGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDwellGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDwellGroupStatus): Boolean {
        when (status) {
            AdaptiveDwellGroupStatus.DRAFT -> {
                when (next) {
                    AdaptiveDwellGroupStatus.PENDING -> return true
                }
                return false
            }
            AdaptiveDwellGroupStatus.PENDING -> return next == AdaptiveDwellGroupStatus.SUSPENDED
            AdaptiveDwellGroupStatus.ACTIVE -> {
                when (next) {
                    AdaptiveDwellGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveDwellGroupStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveDwellGroupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDwellGroupStatus) {
        if (!canMoveTo(next)) {
            error("adaptive dwell group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive dwell group"} $code"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): AdaptiveDwellGroup {
        return AdaptiveDwellGroup(
            code = code,
            name = name,
            graceDays = graceDays,
            category = category,
            dock = dock,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a adaptive dwell group in status draft. */
fun newAdaptiveDwellGroup(code: String, name: String): AdaptiveDwellGroup {
    return AdaptiveDwellGroup(
        code = code,
        name = name,
        graceDays = 0,
        category = "",
        dock = "",
        label = "",
        status = AdaptiveDwellGroupStatus.DRAFT,
    )
}
