package com.acme.transport.crewing.roster.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global roster adjustment is in its life cycle. */
enum class GlobalRosterAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global roster adjustment as the roster team keeps it. */
data class GlobalRosterAdjustment(
    var code: String,
    var title: String,
    var weight: Double,
    var retries: Int,
    var label: String,
    var status: GlobalRosterAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRosterAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRosterAdjustmentStatus): Boolean {
        when (status) {
            GlobalRosterAdjustmentStatus.REQUESTED -> return next == GlobalRosterAdjustmentStatus.GRANTED || next == GlobalRosterAdjustmentStatus.REVOKED
            GlobalRosterAdjustmentStatus.GRANTED -> return next == GlobalRosterAdjustmentStatus.REVOKED
            GlobalRosterAdjustmentStatus.REVOKED -> {
                when (next) {
                    GlobalRosterAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalRosterAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("global roster adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global roster adjustment"} $code"

    fun projectedValue(): Double = round((weight - retries.toDouble()) * 100) / 100

    fun snapshot(): GlobalRosterAdjustment {
        return GlobalRosterAdjustment(
            code = code,
            title = title,
            weight = weight,
            retries = retries,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global roster adjustment in status requested. */
fun newGlobalRosterAdjustment(code: String, title: String): GlobalRosterAdjustment {
    return GlobalRosterAdjustment(
        code = code,
        title = title,
        weight = 0.0,
        retries = 0,
        label = "",
        status = GlobalRosterAdjustmentStatus.REQUESTED,
    )
}
