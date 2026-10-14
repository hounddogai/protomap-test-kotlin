package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual scorecard audit is in its life cycle. */
enum class ManualScorecardAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A manual scorecard audit as the scorecard team keeps it. */
data class ManualScorecardAudit(
    var code: String,
    var name: String,
    var minimum: Double,
    var batchSize: Int,
    var checksum: String,
    var currency: String,
    var updatedAt: Int,
    var maximum: Double,
    var timezone: String,
    var graceDays: Int,
    var archived: Boolean,
    var status: ManualScorecardAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualScorecardAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualScorecardAuditStatus): Boolean {
        when (status) {
            ManualScorecardAuditStatus.OPEN -> return next == ManualScorecardAuditStatus.RESOLVED || next == ManualScorecardAuditStatus.REOPENED
            ManualScorecardAuditStatus.IN_REVIEW -> {
                when (next) {
                    ManualScorecardAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualScorecardAuditStatus.RESOLVED -> {
                when (next) {
                    ManualScorecardAuditStatus.REOPENED, ManualScorecardAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualScorecardAuditStatus.REOPENED -> {
                when (next) {
                    ManualScorecardAuditStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualScorecardAuditStatus) {
        if (!canMoveTo(next)) {
            error("manual scorecard audit $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round((minimum - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): ManualScorecardAudit {
        return ManualScorecardAudit(
            code = code,
            name = name,
            minimum = minimum,
            batchSize = batchSize,
            checksum = checksum,
            currency = currency,
            updatedAt = updatedAt,
            maximum = maximum,
            timezone = timezone,
            graceDays = graceDays,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual scorecard audit in status open. */
fun newManualScorecardAudit(code: String, name: String): ManualScorecardAudit {
    return ManualScorecardAudit(
        code = code,
        name = name,
        minimum = 0.0,
        batchSize = 0,
        checksum = "",
        currency = "",
        updatedAt = 0,
        maximum = 0.0,
        timezone = "",
        graceDays = 0,
        archived = false,
        status = ManualScorecardAuditStatus.OPEN,
    )
}
