package com.acme.transport.linehaul.tender.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global tender digest is in its life cycle. */
enum class GlobalTenderDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One global tender digest, with what the planners need to act on it. */
data class GlobalTenderDigest(
    var code: String,
    var title: String,
    var notes: String,
    var category: String,
    var revision: Int,
    var active: Boolean,
    var reviewer: String,
    var startsAt: Int,
    var status: GlobalTenderDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTenderDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTenderDigestStatus): Boolean {
        when (status) {
            GlobalTenderDigestStatus.IDLE -> return next == GlobalTenderDigestStatus.READY || next == GlobalTenderDigestStatus.STOPPED
            GlobalTenderDigestStatus.WARMING -> {
                when (next) {
                    GlobalTenderDigestStatus.DRAINING, GlobalTenderDigestStatus.STOPPED -> return true
                }
                return false
            }
            GlobalTenderDigestStatus.READY -> return next == GlobalTenderDigestStatus.DRAINING
            GlobalTenderDigestStatus.DRAINING -> {
                when (next) {
                    GlobalTenderDigestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalTenderDigestStatus) {
        if (!canMoveTo(next)) {
            error("global tender digest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun netVolume(): Double = round((revision.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalTenderDigest {
        return GlobalTenderDigest(
            code = code,
            title = title,
            notes = notes,
            category = category,
            revision = revision,
            active = active,
            reviewer = reviewer,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a global tender digest in status idle. */
fun newGlobalTenderDigest(code: String, title: String): GlobalTenderDigest {
    return GlobalTenderDigest(
        code = code,
        title = title,
        notes = "",
        category = "",
        revision = 0,
        active = false,
        reviewer = "",
        startsAt = 0,
        status = GlobalTenderDigestStatus.IDLE,
    )
}
