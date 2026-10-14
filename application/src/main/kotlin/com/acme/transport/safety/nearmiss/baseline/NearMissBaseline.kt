package com.acme.transport.safety.nearmiss.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The near miss baseline records of safety's near miss work, and the rules, stores, and calculations around
// them.

/** Where a near miss baseline is in its life cycle. */
enum class NearMissBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A near miss baseline. Its code is unique within a tenant. */
data class NearMissBaseline(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var version: Int,
    var expiresAt: Int,
    var status: NearMissBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissBaselineStatus): Boolean {
        when (status) {
            NearMissBaselineStatus.PLANNED -> {
                when (next) {
                    NearMissBaselineStatus.SCHEDULED, NearMissBaselineStatus.CANCELLED -> return true
                }
                return false
            }
            NearMissBaselineStatus.SCHEDULED -> return next == NearMissBaselineStatus.CANCELLED
            NearMissBaselineStatus.IN_PROGRESS -> {
                when (next) {
                    NearMissBaselineStatus.COMPLETED, NearMissBaselineStatus.CANCELLED -> return true
                }
                return false
            }
            NearMissBaselineStatus.COMPLETED -> return next == NearMissBaselineStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: NearMissBaselineStatus) {
        if (!canMoveTo(next)) {
            error("near miss baseline $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"near miss baseline"} $code"

    fun weightedVolume(): Double = round(version.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): NearMissBaseline {
        return NearMissBaseline(
            code = code,
            title = title,
            pinned = pinned,
            version = version,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a near miss baseline in status planned. */
fun newNearMissBaseline(code: String, title: String): NearMissBaseline {
    return NearMissBaseline(
        code = code,
        title = title,
        pinned = false,
        version = 0,
        expiresAt = 0,
        status = NearMissBaselineStatus.PLANNED,
    )
}
