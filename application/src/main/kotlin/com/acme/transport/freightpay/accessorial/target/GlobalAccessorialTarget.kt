package com.acme.transport.freightpay.accessorial.target

import com.acme.transport.kit.text.normalize

// The global accessorial target records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a global accessorial target is in its life cycle. */
enum class GlobalAccessorialTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a global accessorial target and its current status. */
data class GlobalAccessorialTarget(
    var code: String,
    var name: String,
    var source: String,
    var title: String,
    var notes: String,
    var reviewer: String,
    var score: Double,
    var status: GlobalAccessorialTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAccessorialTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAccessorialTargetStatus): Boolean {
        when (status) {
            GlobalAccessorialTargetStatus.OPEN -> return next == GlobalAccessorialTargetStatus.ARCHIVED
            GlobalAccessorialTargetStatus.IN_REVIEW -> {
                when (next) {
                    GlobalAccessorialTargetStatus.REOPENED, GlobalAccessorialTargetStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalAccessorialTargetStatus.RESOLVED -> return next == GlobalAccessorialTargetStatus.REOPENED || next == GlobalAccessorialTargetStatus.ARCHIVED
            GlobalAccessorialTargetStatus.REOPENED -> return next == GlobalAccessorialTargetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalAccessorialTargetStatus) {
        if (!canMoveTo(next)) {
            error("global accessorial target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global accessorial target"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): GlobalAccessorialTarget {
        return GlobalAccessorialTarget(
            code = code,
            name = name,
            source = source,
            title = title,
            notes = notes,
            reviewer = reviewer,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global accessorial target in status open. */
fun newGlobalAccessorialTarget(code: String, name: String): GlobalAccessorialTarget {
    return GlobalAccessorialTarget(
        code = code,
        name = name,
        source = "",
        title = "",
        notes = "",
        reviewer = "",
        score = 0.0,
        status = GlobalAccessorialTargetStatus.OPEN,
    )
}
