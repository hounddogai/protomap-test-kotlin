package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global trailer claim is in its life cycle. */
enum class GlobalTrailerClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A global trailer claim. Its code is unique within a tenant. */
data class GlobalTrailerClaim(
    var code: String,
    var title: String,
    var limit: Int,
    var stopCount: Int,
    var archived: Boolean,
    var status: GlobalTrailerClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTrailerClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTrailerClaimStatus): Boolean {
        when (status) {
            GlobalTrailerClaimStatus.CREATED -> {
                when (next) {
                    GlobalTrailerClaimStatus.VALIDATED -> return true
                }
                return false
            }
            GlobalTrailerClaimStatus.VALIDATED -> {
                when (next) {
                    GlobalTrailerClaimStatus.RETIRED -> return true
                }
                return false
            }
            GlobalTrailerClaimStatus.PUBLISHED -> return next == GlobalTrailerClaimStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: GlobalTrailerClaimStatus) {
        if (!canMoveTo(next)) {
            error("global trailer claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedVolume(): Double = round(limit.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): GlobalTrailerClaim {
        return GlobalTrailerClaim(
            code = code,
            title = title,
            limit = limit,
            stopCount = stopCount,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global trailer claim in status created. */
fun newGlobalTrailerClaim(code: String, title: String): GlobalTrailerClaim {
    return GlobalTrailerClaim(
        code = code,
        title = title,
        limit = 0,
        stopCount = 0,
        archived = false,
        status = GlobalTrailerClaimStatus.CREATED,
    )
}
