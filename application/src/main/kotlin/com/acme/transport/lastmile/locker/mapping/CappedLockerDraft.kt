package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped locker draft is in its life cycle. */
enum class CappedLockerDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped locker draft. Its code is unique within a tenant. */
data class CappedLockerDraft(
    var code: String,
    var title: String,
    var contactEmail: String,
    var updatedAt: Int,
    var driverId: String,
    var endsAt: Int,
    var priority: Int,
    var notes: String,
    var status: CappedLockerDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedLockerDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedLockerDraftStatus): Boolean {
        when (status) {
            CappedLockerDraftStatus.REQUESTED -> {
                when (next) {
                    CappedLockerDraftStatus.REVOKED, CappedLockerDraftStatus.EXPIRED -> return true
                }
                return false
            }
            CappedLockerDraftStatus.GRANTED -> return next == CappedLockerDraftStatus.REVOKED || next == CappedLockerDraftStatus.EXPIRED
            CappedLockerDraftStatus.REVOKED -> return next == CappedLockerDraftStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedLockerDraftStatus) {
        if (!canMoveTo(next)) {
            error("capped locker draft $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveValue(): Double = round((priority.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): CappedLockerDraft {
        return CappedLockerDraft(
            code = code,
            title = title,
            contactEmail = contactEmail,
            updatedAt = updatedAt,
            driverId = driverId,
            endsAt = endsAt,
            priority = priority,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped locker draft in status requested. */
fun newCappedLockerDraft(code: String, title: String): CappedLockerDraft {
    return CappedLockerDraft(
        code = code,
        title = title,
        contactEmail = "",
        updatedAt = 0,
        driverId = "",
        endsAt = 0,
        priority = 0,
        notes = "",
        status = CappedLockerDraftStatus.REQUESTED,
    )
}
