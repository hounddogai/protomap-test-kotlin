package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.text.normalize

/** Where a locker draft is in its life cycle. */
enum class LockerDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a locker draft and its current status. */
data class LockerDraft(
    var code: String,
    var label: String,
    var visible: Boolean,
    var externalRef: String,
    var ownerId: String,
    var title: String,
    var locale: String,
    var notes: String,
    var unit: String,
    var trailerId: String,
    var amount: Double,
    var status: LockerDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerDraftStatus): Boolean {
        when (status) {
            LockerDraftStatus.REQUESTED -> {
                when (next) {
                    LockerDraftStatus.REVOKED, LockerDraftStatus.EXPIRED -> return true
                }
                return false
            }
            LockerDraftStatus.GRANTED -> {
                when (next) {
                    LockerDraftStatus.REVOKED, LockerDraftStatus.EXPIRED -> return true
                }
                return false
            }
            LockerDraftStatus.REVOKED -> {
                when (next) {
                    LockerDraftStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerDraftStatus) {
        if (!canMoveTo(next)) {
            error("locker draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = amount

    fun snapshot(): LockerDraft {
        return LockerDraft(
            code = code,
            label = label,
            visible = visible,
            externalRef = externalRef,
            ownerId = ownerId,
            title = title,
            locale = locale,
            notes = notes,
            unit = unit,
            trailerId = trailerId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a locker draft in status requested. */
fun newLockerDraft(code: String, label: String): LockerDraft {
    return LockerDraft(
        code = code,
        label = label,
        visible = false,
        externalRef = "",
        ownerId = "",
        title = "",
        locale = "",
        notes = "",
        unit = "",
        trailerId = "",
        amount = 0.0,
        status = LockerDraftStatus.REQUESTED,
    )
}
