package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.text.normalize

/** Where a cutoff window is in its life cycle. */
enum class CutoffWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A cutoff window. Its code is unique within a tenant. */
data class CutoffWindow(
    var code: String,
    var label: String,
    var lane: String,
    var cost: Double,
    var depot: String,
    var status: CutoffWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffWindowStatus): Boolean {
        when (status) {
            CutoffWindowStatus.DRAFT -> return next == CutoffWindowStatus.SUSPENDED || next == CutoffWindowStatus.CLOSED
            CutoffWindowStatus.PENDING -> {
                when (next) {
                    CutoffWindowStatus.ACTIVE, CutoffWindowStatus.CLOSED -> return true
                }
                return false
            }
            CutoffWindowStatus.ACTIVE -> {
                when (next) {
                    CutoffWindowStatus.SUSPENDED, CutoffWindowStatus.CLOSED -> return true
                }
                return false
            }
            CutoffWindowStatus.SUSPENDED -> return next == CutoffWindowStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CutoffWindowStatus) {
        if (!canMoveTo(next)) {
            error("cutoff window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"cutoff window"} $code"

    fun measuredValue(): Double = cost

    fun snapshot(): CutoffWindow {
        return CutoffWindow(code = code, label = label, lane = lane, cost = cost, depot = depot, status = status)
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a cutoff window in status draft. */
fun newCutoffWindow(code: String, label: String): CutoffWindow {
    return CutoffWindow(
        code = code,
        label = label,
        lane = "",
        cost = 0.0,
        depot = "",
        status = CutoffWindowStatus.DRAFT,
    )
}
