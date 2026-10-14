package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.kit.text.normalize

/** Where a regional lease return window is in its life cycle. */
enum class RegionalLeaseReturnWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One regional lease return window, with what the planners need to act on it. */
data class RegionalLeaseReturnWindow(
    var code: String,
    var name: String,
    var summary: String,
    var tags: MutableList<String>,
    var contactEmail: String,
    var score: Double,
    var status: RegionalLeaseReturnWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalLeaseReturnWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalLeaseReturnWindowStatus): Boolean {
        when (status) {
            RegionalLeaseReturnWindowStatus.NEW -> {
                when (next) {
                    RegionalLeaseReturnWindowStatus.QUEUED -> return true
                }
                return false
            }
            RegionalLeaseReturnWindowStatus.QUEUED -> return next == RegionalLeaseReturnWindowStatus.RUNNING || next == RegionalLeaseReturnWindowStatus.FAILED
            RegionalLeaseReturnWindowStatus.RUNNING -> {
                when (next) {
                    RegionalLeaseReturnWindowStatus.DONE, RegionalLeaseReturnWindowStatus.FAILED -> return true
                }
                return false
            }
            RegionalLeaseReturnWindowStatus.DONE -> return next == RegionalLeaseReturnWindowStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalLeaseReturnWindowStatus) {
        if (!canMoveTo(next)) {
            error("regional lease return window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = score

    fun snapshot(): RegionalLeaseReturnWindow {
        return RegionalLeaseReturnWindow(
            code = code,
            name = name,
            summary = summary,
            tags = tags,
            contactEmail = contactEmail,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional lease return window in status new. */
fun newRegionalLeaseReturnWindow(code: String, name: String): RegionalLeaseReturnWindow {
    return RegionalLeaseReturnWindow(
        code = code,
        name = name,
        summary = "",
        tags = mutableListOf<String>(),
        contactEmail = "",
        score = 0.0,
        status = RegionalLeaseReturnWindowStatus.NEW,
    )
}
