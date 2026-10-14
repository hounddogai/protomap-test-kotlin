package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped dwell draft is in its life cycle. */
enum class CappedDwellDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capped dwell draft as the dwell team keeps it. */
data class CappedDwellDraft(
    var code: String,
    var name: String,
    var limit: Int,
    var approved: Boolean,
    var checksum: String,
    var stopCount: Int,
    var locale: String,
    var status: CappedDwellDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDwellDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDwellDraftStatus): Boolean {
        when (status) {
            CappedDwellDraftStatus.CREATED -> return next == CappedDwellDraftStatus.PUBLISHED
            CappedDwellDraftStatus.VALIDATED -> {
                when (next) {
                    CappedDwellDraftStatus.PUBLISHED, CappedDwellDraftStatus.RETIRED -> return true
                }
                return false
            }
            CappedDwellDraftStatus.PUBLISHED -> {
                when (next) {
                    CappedDwellDraftStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedDwellDraftStatus) {
        if (!canMoveTo(next)) {
            error("capped dwell draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netLoad(): Double = round(stopCount.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): CappedDwellDraft {
        return CappedDwellDraft(
            code = code,
            name = name,
            limit = limit,
            approved = approved,
            checksum = checksum,
            stopCount = stopCount,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a capped dwell draft in status created. */
fun newCappedDwellDraft(code: String, name: String): CappedDwellDraft {
    return CappedDwellDraft(
        code = code,
        name = name,
        limit = 0,
        approved = false,
        checksum = "",
        stopCount = 0,
        locale = "",
        status = CappedDwellDraftStatus.CREATED,
    )
}
