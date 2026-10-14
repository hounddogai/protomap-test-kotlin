package com.acme.transport.crewing.roster.score

import com.acme.transport.kit.text.normalize

/** Where a global roster group is in its life cycle. */
enum class GlobalRosterGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a global roster group and its current status. */
data class GlobalRosterGroup(
    var code: String,
    var title: String,
    var summary: String,
    var dock: String,
    var vehicleId: String,
    var quantity: Int,
    var status: GlobalRosterGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRosterGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRosterGroupStatus): Boolean {
        when (status) {
            GlobalRosterGroupStatus.DRAFT -> return next == GlobalRosterGroupStatus.ACTIVE || next == GlobalRosterGroupStatus.CLOSED
            GlobalRosterGroupStatus.PENDING -> {
                when (next) {
                    GlobalRosterGroupStatus.ACTIVE -> return true
                }
                return false
            }
            GlobalRosterGroupStatus.ACTIVE -> {
                when (next) {
                    GlobalRosterGroupStatus.CLOSED -> return true
                }
                return false
            }
            GlobalRosterGroupStatus.SUSPENDED -> {
                when (next) {
                    GlobalRosterGroupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalRosterGroupStatus) {
        if (!canMoveTo(next)) {
            error("global roster group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): GlobalRosterGroup {
        return GlobalRosterGroup(
            code = code,
            title = title,
            summary = summary,
            dock = dock,
            vehicleId = vehicleId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global roster group in status draft. */
fun newGlobalRosterGroup(code: String, title: String): GlobalRosterGroup {
    return GlobalRosterGroup(
        code = code,
        title = title,
        summary = "",
        dock = "",
        vehicleId = "",
        quantity = 0,
        status = GlobalRosterGroupStatus.DRAFT,
    )
}
