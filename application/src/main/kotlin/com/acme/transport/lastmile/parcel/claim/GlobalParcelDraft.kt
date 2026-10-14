package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global parcel draft is in its life cycle. */
enum class GlobalParcelDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A global parcel draft as the parcel team keeps it. */
data class GlobalParcelDraft(
    var code: String,
    var name: String,
    var graceDays: Int,
    var summary: String,
    var updatedAt: Int,
    var currency: String,
    var expiresAt: Int,
    var notes: String,
    var status: GlobalParcelDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalParcelDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalParcelDraftStatus): Boolean {
        when (status) {
            GlobalParcelDraftStatus.CREATED -> {
                when (next) {
                    GlobalParcelDraftStatus.RETIRED -> return true
                }
                return false
            }
            GlobalParcelDraftStatus.VALIDATED -> return next == GlobalParcelDraftStatus.PUBLISHED || next == GlobalParcelDraftStatus.RETIRED
            GlobalParcelDraftStatus.PUBLISHED -> return next == GlobalParcelDraftStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: GlobalParcelDraftStatus) {
        if (!canMoveTo(next)) {
            error("global parcel draft $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedValue(): Double = round((expiresAt.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): GlobalParcelDraft {
        return GlobalParcelDraft(
            code = code,
            name = name,
            graceDays = graceDays,
            summary = summary,
            updatedAt = updatedAt,
            currency = currency,
            expiresAt = expiresAt,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global parcel draft in status created. */
fun newGlobalParcelDraft(code: String, name: String): GlobalParcelDraft {
    return GlobalParcelDraft(
        code = code,
        name = name,
        graceDays = 0,
        summary = "",
        updatedAt = 0,
        currency = "",
        expiresAt = 0,
        notes = "",
        status = GlobalParcelDraftStatus.CREATED,
    )
}
