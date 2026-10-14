package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell baseline is in its life cycle. */
enum class DwellBaselineStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A dwell baseline as the dwell team keeps it. */
data class DwellBaseline(
    var code: String,
    var title: String,
    var floorValue: Double,
    var active: Boolean,
    var rate: Double,
    var minimum: Double,
    var sequence: Int,
    var depot: String,
    var revision: Int,
    var region: String,
    var trailerId: String,
    var status: DwellBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellBaselineStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellBaselineStatus): Boolean {
        when (status) {
            DwellBaselineStatus.PROPOSED -> {
                when (next) {
                    DwellBaselineStatus.APPROVED, DwellBaselineStatus.WITHDRAWN -> return true
                }
                return false
            }
            DwellBaselineStatus.APPROVED -> return next == DwellBaselineStatus.WITHDRAWN
            DwellBaselineStatus.REJECTED -> return next == DwellBaselineStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DwellBaselineStatus) {
        if (!canMoveTo(next)) {
            error("dwell baseline $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun projectedTotal(): Double = round((minimum + rate) * 100) / 100

    fun snapshot(): DwellBaseline {
        return DwellBaseline(
            code = code,
            title = title,
            floorValue = floorValue,
            active = active,
            rate = rate,
            minimum = minimum,
            sequence = sequence,
            depot = depot,
            revision = revision,
            region = region,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a dwell baseline in status proposed. */
fun newDwellBaseline(code: String, title: String): DwellBaseline {
    return DwellBaseline(
        code = code,
        title = title,
        floorValue = 0.0,
        active = false,
        rate = 0.0,
        minimum = 0.0,
        sequence = 0,
        depot = "",
        revision = 0,
        region = "",
        trailerId = "",
        status = DwellBaselineStatus.PROPOSED,
    )
}
