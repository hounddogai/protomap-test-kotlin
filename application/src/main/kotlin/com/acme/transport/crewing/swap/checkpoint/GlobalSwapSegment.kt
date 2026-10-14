package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global swap segment is in its life cycle. */
enum class GlobalSwapSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global swap segment as the swap team keeps it. */
data class GlobalSwapSegment(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var ratio: Double,
    var sequence: Int,
    var etaMinutes: Int,
    var status: GlobalSwapSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalSwapSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalSwapSegmentStatus): Boolean {
        when (status) {
            GlobalSwapSegmentStatus.PROPOSED -> {
                when (next) {
                    GlobalSwapSegmentStatus.REJECTED, GlobalSwapSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalSwapSegmentStatus.APPROVED -> {
                when (next) {
                    GlobalSwapSegmentStatus.REJECTED, GlobalSwapSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalSwapSegmentStatus.REJECTED -> return next == GlobalSwapSegmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalSwapSegmentStatus) {
        if (!canMoveTo(next)) {
            error("global swap segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netTotal(): Double = round(etaMinutes.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): GlobalSwapSegment {
        return GlobalSwapSegment(
            code = code,
            title = title,
            pinned = pinned,
            ratio = ratio,
            sequence = sequence,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global swap segment in status proposed. */
fun newGlobalSwapSegment(code: String, title: String): GlobalSwapSegment {
    return GlobalSwapSegment(
        code = code,
        title = title,
        pinned = false,
        ratio = 0.0,
        sequence = 0,
        etaMinutes = 0,
        status = GlobalSwapSegmentStatus.PROPOSED,
    )
}
