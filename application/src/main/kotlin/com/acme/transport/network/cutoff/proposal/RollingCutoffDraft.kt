package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling cutoff draft is in its life cycle. */
enum class RollingCutoffDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rolling cutoff draft as the cutoff team keeps it. */
data class RollingCutoffDraft(
    var code: String,
    var name: String,
    var description: String,
    var visible: Boolean,
    var contactEmail: String,
    var revision: Int,
    var locale: String,
    var threshold: Double,
    var region: String,
    var discount: Double,
    var status: RollingCutoffDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCutoffDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCutoffDraftStatus): Boolean {
        when (status) {
            RollingCutoffDraftStatus.DRAFT -> return next == RollingCutoffDraftStatus.ACTIVE || next == RollingCutoffDraftStatus.CLOSED
            RollingCutoffDraftStatus.PENDING -> return next == RollingCutoffDraftStatus.SUSPENDED || next == RollingCutoffDraftStatus.CLOSED
            RollingCutoffDraftStatus.ACTIVE -> return next == RollingCutoffDraftStatus.SUSPENDED
            RollingCutoffDraftStatus.SUSPENDED -> return next == RollingCutoffDraftStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingCutoffDraftStatus) {
        if (!canMoveTo(next)) {
            error("rolling cutoff draft $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"rolling cutoff draft"} $code"

    fun adjustedLoad(): Double = round(revision.toDouble() * threshold * 100) / 100

    fun snapshot(): RollingCutoffDraft {
        return RollingCutoffDraft(
            code = code,
            name = name,
            description = description,
            visible = visible,
            contactEmail = contactEmail,
            revision = revision,
            locale = locale,
            threshold = threshold,
            region = region,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling cutoff draft in status draft. */
fun newRollingCutoffDraft(code: String, name: String): RollingCutoffDraft {
    return RollingCutoffDraft(
        code = code,
        name = name,
        description = "",
        visible = false,
        contactEmail = "",
        revision = 0,
        locale = "",
        threshold = 0.0,
        region = "",
        discount = 0.0,
        status = RollingCutoffDraftStatus.DRAFT,
    )
}
