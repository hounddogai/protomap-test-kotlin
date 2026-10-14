package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending cross dock score is in its life cycle. */
enum class PendingCrossDockScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending cross dock score as the cross dock team keeps it. */
data class PendingCrossDockScore(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var revision: Int,
    var updatedAt: Int,
    var version: Int,
    var status: PendingCrossDockScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCrossDockScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCrossDockScoreStatus): Boolean {
        when (status) {
            PendingCrossDockScoreStatus.OPEN -> return next == PendingCrossDockScoreStatus.ARCHIVED
            PendingCrossDockScoreStatus.IN_REVIEW -> {
                when (next) {
                    PendingCrossDockScoreStatus.RESOLVED, PendingCrossDockScoreStatus.REOPENED -> return true
                }
                return false
            }
            PendingCrossDockScoreStatus.RESOLVED -> {
                when (next) {
                    PendingCrossDockScoreStatus.REOPENED, PendingCrossDockScoreStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingCrossDockScoreStatus.REOPENED -> {
                when (next) {
                    PendingCrossDockScoreStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingCrossDockScoreStatus) {
        if (!canMoveTo(next)) {
            error("pending cross dock score $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"pending cross dock score"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round(updatedAt.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): PendingCrossDockScore {
        return PendingCrossDockScore(
            code = code,
            title = title,
            attributes = attributes,
            revision = revision,
            updatedAt = updatedAt,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending cross dock score in status open. */
fun newPendingCrossDockScore(code: String, title: String): PendingCrossDockScore {
    return PendingCrossDockScore(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        revision = 0,
        updatedAt = 0,
        version = 0,
        status = PendingCrossDockScoreStatus.OPEN,
    )
}
