package com.acme.transport.terminal.crossdock.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cross dock claim records of terminal's cross dock work, and the rules, stores, and calculations around
// them.

/** Where a cross dock claim is in its life cycle. */
enum class CrossDockClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One cross dock claim, with what the planners need to act on it. */
data class CrossDockClaim(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var trailerId: String,
    var description: String,
    var endsAt: Int,
    var status: CrossDockClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockClaimStatus): Boolean {
        when (status) {
            CrossDockClaimStatus.NEW -> {
                when (next) {
                    CrossDockClaimStatus.QUEUED -> return true
                }
                return false
            }
            CrossDockClaimStatus.QUEUED -> {
                when (next) {
                    CrossDockClaimStatus.FAILED -> return true
                }
                return false
            }
            CrossDockClaimStatus.RUNNING -> return next == CrossDockClaimStatus.DONE || next == CrossDockClaimStatus.FAILED
            CrossDockClaimStatus.DONE -> return next == CrossDockClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CrossDockClaimStatus) {
        if (!canMoveTo(next)) {
            error("cross dock claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun weightedTotal(): Double = round(expiresAt.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): CrossDockClaim {
        return CrossDockClaim(
            code = code,
            title = title,
            expiresAt = expiresAt,
            trailerId = trailerId,
            description = description,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cross dock claim in status new. */
fun newCrossDockClaim(code: String, title: String): CrossDockClaim {
    return CrossDockClaim(
        code = code,
        title = title,
        expiresAt = 0,
        trailerId = "",
        description = "",
        endsAt = 0,
        status = CrossDockClaimStatus.NEW,
    )
}
