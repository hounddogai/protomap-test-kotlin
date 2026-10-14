package com.acme.transport.safety.scorecard.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard audit is in its life cycle. */
enum class ScorecardAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scorecard audit. Its code is unique within a tenant. */
data class ScorecardAudit(
    var code: String,
    var title: String,
    var contactEmail: String,
    var endsAt: Int,
    var archived: Boolean,
    var locale: String,
    var visible: Boolean,
    var rate: Double,
    var tags: MutableList<String>,
    var fuelLiters: Double,
    var batchSize: Int,
    var status: ScorecardAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardAuditStatus): Boolean {
        when (status) {
            ScorecardAuditStatus.OPEN -> return next == ScorecardAuditStatus.REOPENED || next == ScorecardAuditStatus.ARCHIVED
            ScorecardAuditStatus.IN_REVIEW -> return next == ScorecardAuditStatus.RESOLVED
            ScorecardAuditStatus.RESOLVED -> {
                when (next) {
                    ScorecardAuditStatus.REOPENED -> return true
                }
                return false
            }
            ScorecardAuditStatus.REOPENED -> return next == ScorecardAuditStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScorecardAuditStatus) {
        if (!canMoveTo(next)) {
            error("scorecard audit $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netTotal(): Double = round((fuelLiters - endsAt.toDouble()) * 100) / 100

    fun snapshot(): ScorecardAudit {
        return ScorecardAudit(
            code = code,
            title = title,
            contactEmail = contactEmail,
            endsAt = endsAt,
            archived = archived,
            locale = locale,
            visible = visible,
            rate = rate,
            tags = tags,
            fuelLiters = fuelLiters,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scorecard audit in status open. */
fun newScorecardAudit(code: String, title: String): ScorecardAudit {
    return ScorecardAudit(
        code = code,
        title = title,
        contactEmail = "",
        endsAt = 0,
        archived = false,
        locale = "",
        visible = false,
        rate = 0.0,
        tags = mutableListOf<String>(),
        fuelLiters = 0.0,
        batchSize = 0,
        status = ScorecardAuditStatus.OPEN,
    )
}
