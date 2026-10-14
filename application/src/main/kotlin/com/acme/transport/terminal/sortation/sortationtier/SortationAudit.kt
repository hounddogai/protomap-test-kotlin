package com.acme.transport.terminal.sortation.sortationtier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation audit is in its life cycle. */
enum class SortationAuditStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a sortation audit and its current status. */
data class SortationAudit(
    var code: String,
    var name: String,
    var depot: String,
    var distanceKm: Double,
    var loadKg: Double,
    var status: SortationAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationAuditStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationAuditStatus): Boolean {
        when (status) {
            SortationAuditStatus.CREATED -> {
                when (next) {
                    SortationAuditStatus.RETIRED -> return true
                }
                return false
            }
            SortationAuditStatus.VALIDATED -> return next == SortationAuditStatus.PUBLISHED || next == SortationAuditStatus.RETIRED
            SortationAuditStatus.PUBLISHED -> return next == SortationAuditStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SortationAuditStatus) {
        if (!canMoveTo(next)) {
            error("sortation audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"sortation audit"} $code"

    fun effectiveExposure(): Double = round(distanceKm * loadKg * 100) / 100

    fun snapshot(): SortationAudit {
        return SortationAudit(
            code = code,
            name = name,
            depot = depot,
            distanceKm = distanceKm,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a sortation audit in status created. */
fun newSortationAudit(code: String, name: String): SortationAudit {
    return SortationAudit(
        code = code,
        name = name,
        depot = "",
        distanceKm = 0.0,
        loadKg = 0.0,
        status = SortationAuditStatus.CREATED,
    )
}
