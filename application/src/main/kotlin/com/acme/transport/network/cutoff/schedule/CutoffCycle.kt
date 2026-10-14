package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.text.normalize

/** Where a cutoff cycle is in its life cycle. */
enum class CutoffCycleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One cutoff cycle, with what the planners need to act on it. */
data class CutoffCycle(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var name: String,
    var locale: String,
    var title: String,
    var quantity: Int,
    var status: CutoffCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffCycleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffCycleStatus): Boolean {
        when (status) {
            CutoffCycleStatus.OPEN -> {
                when (next) {
                    CutoffCycleStatus.IN_REVIEW, CutoffCycleStatus.REOPENED -> return true
                }
                return false
            }
            CutoffCycleStatus.IN_REVIEW -> return next == CutoffCycleStatus.ARCHIVED
            CutoffCycleStatus.RESOLVED -> {
                when (next) {
                    CutoffCycleStatus.REOPENED -> return true
                }
                return false
            }
            CutoffCycleStatus.REOPENED -> {
                when (next) {
                    CutoffCycleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffCycleStatus) {
        if (!canMoveTo(next)) {
            error("cutoff cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): CutoffCycle {
        return CutoffCycle(
            code = code,
            label = label,
            tags = tags,
            name = name,
            locale = locale,
            title = title,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a cutoff cycle in status open. */
fun newCutoffCycle(code: String, label: String): CutoffCycle {
    return CutoffCycle(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        name = "",
        locale = "",
        title = "",
        quantity = 0,
        status = CutoffCycleStatus.OPEN,
    )
}
