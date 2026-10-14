package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary locker draft is in its life cycle. */
enum class PrimaryLockerDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A primary locker draft as the locker team keeps it. */
data class PrimaryLockerDraft(
    var code: String,
    var title: String,
    var trailerId: String,
    var approved: Boolean,
    var attributes: MutableMap<String, String>,
    var source: String,
    var batchSize: Int,
    var graceDays: Int,
    var status: PrimaryLockerDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryLockerDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryLockerDraftStatus): Boolean {
        when (status) {
            PrimaryLockerDraftStatus.NEW -> return next == PrimaryLockerDraftStatus.RUNNING
            PrimaryLockerDraftStatus.QUEUED -> {
                when (next) {
                    PrimaryLockerDraftStatus.FAILED -> return true
                }
                return false
            }
            PrimaryLockerDraftStatus.RUNNING -> {
                when (next) {
                    PrimaryLockerDraftStatus.DONE -> return true
                }
                return false
            }
            PrimaryLockerDraftStatus.DONE -> {
                when (next) {
                    PrimaryLockerDraftStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryLockerDraftStatus) {
        if (!canMoveTo(next)) {
            error("primary locker draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary locker draft"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round(batchSize.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): PrimaryLockerDraft {
        return PrimaryLockerDraft(
            code = code,
            title = title,
            trailerId = trailerId,
            approved = approved,
            attributes = attributes,
            source = source,
            batchSize = batchSize,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a primary locker draft in status new. */
fun newPrimaryLockerDraft(code: String, title: String): PrimaryLockerDraft {
    return PrimaryLockerDraft(
        code = code,
        title = title,
        trailerId = "",
        approved = false,
        attributes = mutableMapOf<String, String>(),
        source = "",
        batchSize = 0,
        graceDays = 0,
        status = PrimaryLockerDraftStatus.NEW,
    )
}
