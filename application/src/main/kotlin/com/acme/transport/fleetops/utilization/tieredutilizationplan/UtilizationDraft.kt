package com.acme.transport.fleetops.utilization.tieredutilizationplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization draft is in its life cycle. */
enum class UtilizationDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One utilization draft, with what the planners need to act on it. */
data class UtilizationDraft(
    var code: String,
    var label: String,
    var carrier: String,
    var approved: Boolean,
    var amount: Double,
    var active: Boolean,
    var routeCode: String,
    var version: Int,
    var driverId: String,
    var status: UtilizationDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationDraftStatus): Boolean {
        when (status) {
            UtilizationDraftStatus.REQUESTED -> return next == UtilizationDraftStatus.REVOKED
            UtilizationDraftStatus.GRANTED -> {
                when (next) {
                    UtilizationDraftStatus.EXPIRED -> return true
                }
                return false
            }
            UtilizationDraftStatus.REVOKED -> {
                when (next) {
                    UtilizationDraftStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationDraftStatus) {
        if (!canMoveTo(next)) {
            error("utilization draft $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedLoad(): Double = round(version.toDouble() * amount * 100) / 100

    fun snapshot(): UtilizationDraft {
        return UtilizationDraft(
            code = code,
            label = label,
            carrier = carrier,
            approved = approved,
            amount = amount,
            active = active,
            routeCode = routeCode,
            version = version,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a utilization draft in status requested. */
fun newUtilizationDraft(code: String, label: String): UtilizationDraft {
    return UtilizationDraft(
        code = code,
        label = label,
        carrier = "",
        approved = false,
        amount = 0.0,
        active = false,
        routeCode = "",
        version = 0,
        driverId = "",
        status = UtilizationDraftStatus.REQUESTED,
    )
}
