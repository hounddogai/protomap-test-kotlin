package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.text.normalize

/** Where a drop baseline is in its life cycle. */
enum class DropBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A drop baseline as the drop team keeps it. */
data class DropBaseline(
    var code: String,
    var label: String,
    var notes: String,
    var rate: Double,
    var title: String,
    var approved: Boolean,
    var segment: String,
    var status: DropBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropBaselineStatus): Boolean {
        when (status) {
            DropBaselineStatus.REQUESTED -> {
                when (next) {
                    DropBaselineStatus.GRANTED, DropBaselineStatus.REVOKED -> return true
                }
                return false
            }
            DropBaselineStatus.GRANTED -> return next == DropBaselineStatus.EXPIRED
            DropBaselineStatus.REVOKED -> {
                when (next) {
                    DropBaselineStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropBaselineStatus) {
        if (!canMoveTo(next)) {
            error("drop baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = rate

    fun snapshot(): DropBaseline {
        return DropBaseline(
            code = code,
            label = label,
            notes = notes,
            rate = rate,
            title = title,
            approved = approved,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a drop baseline in status requested. */
fun newDropBaseline(code: String, label: String): DropBaseline {
    return DropBaseline(
        code = code,
        label = label,
        notes = "",
        rate = 0.0,
        title = "",
        approved = false,
        segment = "",
        status = DropBaselineStatus.REQUESTED,
    )
}
