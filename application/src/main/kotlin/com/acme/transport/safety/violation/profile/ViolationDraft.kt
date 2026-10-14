package com.acme.transport.safety.violation.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation draft is in its life cycle. */
enum class ViolationDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A violation draft. Its code is unique within a tenant. */
data class ViolationDraft(
    var code: String,
    var label: String,
    var notes: String,
    var expiresAt: Int,
    var contactEmail: String,
    var trailerId: String,
    var pinned: Boolean,
    var ceiling: Double,
    var region: String,
    var status: ViolationDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationDraftStatus): Boolean {
        when (status) {
            ViolationDraftStatus.DRAFT -> return next == ViolationDraftStatus.PENDING
            ViolationDraftStatus.PENDING -> return next == ViolationDraftStatus.ACTIVE || next == ViolationDraftStatus.CLOSED
            ViolationDraftStatus.ACTIVE -> return next == ViolationDraftStatus.SUSPENDED || next == ViolationDraftStatus.CLOSED
            ViolationDraftStatus.SUSPENDED -> {
                when (next) {
                    ViolationDraftStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationDraftStatus) {
        if (!canMoveTo(next)) {
            error("violation draft $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedVolume(): Double = round(expiresAt.toDouble() * ceiling * 100) / 100

    fun snapshot(): ViolationDraft {
        return ViolationDraft(
            code = code,
            label = label,
            notes = notes,
            expiresAt = expiresAt,
            contactEmail = contactEmail,
            trailerId = trailerId,
            pinned = pinned,
            ceiling = ceiling,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a violation draft in status draft. */
fun newViolationDraft(code: String, label: String): ViolationDraft {
    return ViolationDraft(
        code = code,
        label = label,
        notes = "",
        expiresAt = 0,
        contactEmail = "",
        trailerId = "",
        pinned = false,
        ceiling = 0.0,
        region = "",
        status = ViolationDraftStatus.DRAFT,
    )
}
