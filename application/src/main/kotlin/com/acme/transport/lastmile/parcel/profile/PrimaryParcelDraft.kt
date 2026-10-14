package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary parcel draft is in its life cycle. */
enum class PrimaryParcelDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A primary parcel draft as the parcel team keeps it. */
data class PrimaryParcelDraft(
    var code: String,
    var label: String,
    var maximum: Double,
    var pinned: Boolean,
    var threshold: Double,
    var active: Boolean,
    var timezone: String,
    var priority: Int,
    var contactEmail: String,
    var ownerId: String,
    var status: PrimaryParcelDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryParcelDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryParcelDraftStatus): Boolean {
        when (status) {
            PrimaryParcelDraftStatus.DRAFT -> return next == PrimaryParcelDraftStatus.SUSPENDED || next == PrimaryParcelDraftStatus.CLOSED
            PrimaryParcelDraftStatus.PENDING -> {
                when (next) {
                    PrimaryParcelDraftStatus.ACTIVE, PrimaryParcelDraftStatus.CLOSED -> return true
                }
                return false
            }
            PrimaryParcelDraftStatus.ACTIVE -> return next == PrimaryParcelDraftStatus.SUSPENDED
            PrimaryParcelDraftStatus.SUSPENDED -> return next == PrimaryParcelDraftStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PrimaryParcelDraftStatus) {
        if (!canMoveTo(next)) {
            error("primary parcel draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedLoad(): Double = round(maximum * threshold * 100) / 100

    fun snapshot(): PrimaryParcelDraft {
        return PrimaryParcelDraft(
            code = code,
            label = label,
            maximum = maximum,
            pinned = pinned,
            threshold = threshold,
            active = active,
            timezone = timezone,
            priority = priority,
            contactEmail = contactEmail,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary parcel draft in status draft. */
fun newPrimaryParcelDraft(code: String, label: String): PrimaryParcelDraft {
    return PrimaryParcelDraft(
        code = code,
        label = label,
        maximum = 0.0,
        pinned = false,
        threshold = 0.0,
        active = false,
        timezone = "",
        priority = 0,
        contactEmail = "",
        ownerId = "",
        status = PrimaryParcelDraftStatus.DRAFT,
    )
}
