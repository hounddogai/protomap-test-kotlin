package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal redelivery audit is in its life cycle. */
enum class SeasonalRedeliveryAuditStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal redelivery audit as the redelivery team keeps it. */
data class SeasonalRedeliveryAudit(
    var code: String,
    var label: String,
    var priority: Int,
    var unit: String,
    var weight: Double,
    var externalRef: String,
    var status: SeasonalRedeliveryAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRedeliveryAuditStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRedeliveryAuditStatus): Boolean {
        when (status) {
            SeasonalRedeliveryAuditStatus.REQUESTED -> return next == SeasonalRedeliveryAuditStatus.GRANTED
            SeasonalRedeliveryAuditStatus.GRANTED -> return next == SeasonalRedeliveryAuditStatus.EXPIRED
            SeasonalRedeliveryAuditStatus.REVOKED -> return next == SeasonalRedeliveryAuditStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalRedeliveryAuditStatus) {
        if (!canMoveTo(next)) {
            error("seasonal redelivery audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal redelivery audit"} $code"

    fun weightedVolume(): Double = round(weight * priority.toDouble() * 100) / 100

    fun snapshot(): SeasonalRedeliveryAudit {
        return SeasonalRedeliveryAudit(
            code = code,
            label = label,
            priority = priority,
            unit = unit,
            weight = weight,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a seasonal redelivery audit in status requested. */
fun newSeasonalRedeliveryAudit(code: String, label: String): SeasonalRedeliveryAudit {
    return SeasonalRedeliveryAudit(
        code = code,
        label = label,
        priority = 0,
        unit = "",
        weight = 0.0,
        externalRef = "",
        status = SeasonalRedeliveryAuditStatus.REQUESTED,
    )
}
