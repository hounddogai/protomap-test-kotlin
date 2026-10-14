package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global attempt draft records of lastmile's attempt work, and the rules, stores, and calculations around
// them.

/** Where a global attempt draft is in its life cycle. */
enum class GlobalAttemptDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a global attempt draft and its current status. */
data class GlobalAttemptDraft(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var vehicleId: String,
    var updatedAt: Int,
    var checksum: String,
    var loadKg: Double,
    var sequence: Int,
    var status: GlobalAttemptDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAttemptDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAttemptDraftStatus): Boolean {
        when (status) {
            GlobalAttemptDraftStatus.REQUESTED -> return next == GlobalAttemptDraftStatus.REVOKED || next == GlobalAttemptDraftStatus.EXPIRED
            GlobalAttemptDraftStatus.GRANTED -> return next == GlobalAttemptDraftStatus.REVOKED || next == GlobalAttemptDraftStatus.EXPIRED
            GlobalAttemptDraftStatus.REVOKED -> return next == GlobalAttemptDraftStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalAttemptDraftStatus) {
        if (!canMoveTo(next)) {
            error("global attempt draft $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedValue(): Double = round(expiresAt.toDouble() * loadKg * 100) / 100

    fun snapshot(): GlobalAttemptDraft {
        return GlobalAttemptDraft(
            code = code,
            title = title,
            expiresAt = expiresAt,
            vehicleId = vehicleId,
            updatedAt = updatedAt,
            checksum = checksum,
            loadKg = loadKg,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global attempt draft in status requested. */
fun newGlobalAttemptDraft(code: String, title: String): GlobalAttemptDraft {
    return GlobalAttemptDraft(
        code = code,
        title = title,
        expiresAt = 0,
        vehicleId = "",
        updatedAt = 0,
        checksum = "",
        loadKg = 0.0,
        sequence = 0,
        status = GlobalAttemptDraftStatus.REQUESTED,
    )
}
