package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional scorecard audit is in its life cycle. */
enum class RegionalScorecardAuditStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional scorecard audit as the scorecard team keeps it. */
data class RegionalScorecardAudit(
    var code: String,
    var name: String,
    var batchSize: Int,
    var pallets: Int,
    var currency: String,
    var status: RegionalScorecardAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalScorecardAuditStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalScorecardAuditStatus): Boolean {
        when (status) {
            RegionalScorecardAuditStatus.CREATED -> {
                when (next) {
                    RegionalScorecardAuditStatus.VALIDATED, RegionalScorecardAuditStatus.PUBLISHED -> return true
                }
                return false
            }
            RegionalScorecardAuditStatus.VALIDATED -> {
                when (next) {
                    RegionalScorecardAuditStatus.PUBLISHED, RegionalScorecardAuditStatus.RETIRED -> return true
                }
                return false
            }
            RegionalScorecardAuditStatus.PUBLISHED -> return next == RegionalScorecardAuditStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalScorecardAuditStatus) {
        if (!canMoveTo(next)) {
            error("regional scorecard audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netTotal(): Double = round(pallets.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): RegionalScorecardAudit {
        return RegionalScorecardAudit(
            code = code,
            name = name,
            batchSize = batchSize,
            pallets = pallets,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional scorecard audit in status created. */
fun newRegionalScorecardAudit(code: String, name: String): RegionalScorecardAudit {
    return RegionalScorecardAudit(
        code = code,
        name = name,
        batchSize = 0,
        pallets = 0,
        currency = "",
        status = RegionalScorecardAuditStatus.CREATED,
    )
}
