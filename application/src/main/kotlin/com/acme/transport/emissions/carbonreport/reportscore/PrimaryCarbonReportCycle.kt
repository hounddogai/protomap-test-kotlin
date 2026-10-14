package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary carbon report cycle is in its life cycle. */
enum class PrimaryCarbonReportCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary carbon report cycle as the carbon report team keeps it. */
data class PrimaryCarbonReportCycle(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var revision: Int,
    var checksum: String,
    var visible: Boolean,
    var ownerId: String,
    var cost: Double,
    var limit: Int,
    var routeCode: String,
    var createdAt: Int,
    var status: PrimaryCarbonReportCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCarbonReportCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCarbonReportCycleStatus): Boolean {
        when (status) {
            PrimaryCarbonReportCycleStatus.PROPOSED -> return next == PrimaryCarbonReportCycleStatus.REJECTED
            PrimaryCarbonReportCycleStatus.APPROVED -> return next == PrimaryCarbonReportCycleStatus.REJECTED || next == PrimaryCarbonReportCycleStatus.WITHDRAWN
            PrimaryCarbonReportCycleStatus.REJECTED -> {
                when (next) {
                    PrimaryCarbonReportCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryCarbonReportCycleStatus) {
        if (!canMoveTo(next)) {
            error("primary carbon report cycle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"primary carbon report cycle"} $code"

    fun adjustedTotal(): Double = round((cost - revision.toDouble()) * 100) / 100

    fun snapshot(): PrimaryCarbonReportCycle {
        return PrimaryCarbonReportCycle(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            revision = revision,
            checksum = checksum,
            visible = visible,
            ownerId = ownerId,
            cost = cost,
            limit = limit,
            routeCode = routeCode,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary carbon report cycle in status proposed. */
fun newPrimaryCarbonReportCycle(code: String, name: String): PrimaryCarbonReportCycle {
    return PrimaryCarbonReportCycle(
        code = code,
        name = name,
        etaMinutes = 0,
        revision = 0,
        checksum = "",
        visible = false,
        ownerId = "",
        cost = 0.0,
        limit = 0,
        routeCode = "",
        createdAt = 0,
        status = PrimaryCarbonReportCycleStatus.PROPOSED,
    )
}
