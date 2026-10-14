package com.acme.transport.network.transittime.forecast

import com.acme.transport.kit.text.normalize

/** Where a tiered transit time audit is in its life cycle. */
enum class TieredTransitTimeAuditStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One tiered transit time audit, with what the planners need to act on it. */
data class TieredTransitTimeAudit(
    var code: String,
    var title: String,
    var unit: String,
    var segment: String,
    var budget: Double,
    var summary: String,
    var status: TieredTransitTimeAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTransitTimeAuditStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTransitTimeAuditStatus): Boolean {
        when (status) {
            TieredTransitTimeAuditStatus.CREATED -> return next == TieredTransitTimeAuditStatus.RETIRED
            TieredTransitTimeAuditStatus.VALIDATED -> return next == TieredTransitTimeAuditStatus.PUBLISHED || next == TieredTransitTimeAuditStatus.RETIRED
            TieredTransitTimeAuditStatus.PUBLISHED -> {
                when (next) {
                    TieredTransitTimeAuditStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredTransitTimeAuditStatus) {
        if (!canMoveTo(next)) {
            error("tiered transit time audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered transit time audit"} $code"

    fun measuredValue(): Double = budget

    fun snapshot(): TieredTransitTimeAudit {
        return TieredTransitTimeAudit(
            code = code,
            title = title,
            unit = unit,
            segment = segment,
            budget = budget,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tiered transit time audit in status created. */
fun newTieredTransitTimeAudit(code: String, title: String): TieredTransitTimeAudit {
    return TieredTransitTimeAudit(
        code = code,
        title = title,
        unit = "",
        segment = "",
        budget = 0.0,
        summary = "",
        status = TieredTransitTimeAuditStatus.CREATED,
    )
}
