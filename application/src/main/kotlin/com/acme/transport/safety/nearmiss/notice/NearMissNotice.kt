package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The near miss notice records of safety's near miss work, and the rules, stores, and calculations around
// them.

/** Where a near miss notice is in its life cycle. */
enum class NearMissNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A near miss notice. Its code is unique within a tenant. */
data class NearMissNotice(
    var code: String,
    var title: String,
    var reviewer: String,
    var sequence: Int,
    var approved: Boolean,
    var ownerId: String,
    var limit: Int,
    var status: NearMissNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissNoticeStatus): Boolean {
        when (status) {
            NearMissNoticeStatus.PROPOSED -> {
                when (next) {
                    NearMissNoticeStatus.APPROVED -> return true
                }
                return false
            }
            NearMissNoticeStatus.APPROVED -> return next == NearMissNoticeStatus.REJECTED || next == NearMissNoticeStatus.WITHDRAWN
            NearMissNoticeStatus.REJECTED -> {
                when (next) {
                    NearMissNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissNoticeStatus) {
        if (!canMoveTo(next)) {
            error("near miss notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedTotal(): Double = round(sequence.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): NearMissNotice {
        return NearMissNotice(
            code = code,
            title = title,
            reviewer = reviewer,
            sequence = sequence,
            approved = approved,
            ownerId = ownerId,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a near miss notice in status proposed. */
fun newNearMissNotice(code: String, title: String): NearMissNotice {
    return NearMissNotice(
        code = code,
        title = title,
        reviewer = "",
        sequence = 0,
        approved = false,
        ownerId = "",
        limit = 0,
        status = NearMissNoticeStatus.PROPOSED,
    )
}
