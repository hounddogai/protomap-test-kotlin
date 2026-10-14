package com.acme.transport.network.zone.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling zone draft is in its life cycle. */
enum class RollingZoneDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling zone draft as the zone team keeps it. */
data class RollingZoneDraft(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var name: String,
    var expiresAt: Int,
    var status: RollingZoneDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingZoneDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingZoneDraftStatus): Boolean {
        when (status) {
            RollingZoneDraftStatus.NEW -> {
                when (next) {
                    RollingZoneDraftStatus.QUEUED, RollingZoneDraftStatus.DONE -> return true
                }
                return false
            }
            RollingZoneDraftStatus.QUEUED -> return next == RollingZoneDraftStatus.DONE
            RollingZoneDraftStatus.RUNNING -> return next == RollingZoneDraftStatus.DONE || next == RollingZoneDraftStatus.FAILED
            RollingZoneDraftStatus.DONE -> {
                when (next) {
                    RollingZoneDraftStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingZoneDraftStatus) {
        if (!canMoveTo(next)) {
            error("rolling zone draft $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rolling zone draft"} $code"

    fun projectedValue(): Double = round((expiresAt.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): RollingZoneDraft {
        return RollingZoneDraft(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            name = name,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling zone draft in status new. */
fun newRollingZoneDraft(code: String, title: String): RollingZoneDraft {
    return RollingZoneDraft(
        code = code,
        title = title,
        durationMinutes = 0,
        name = "",
        expiresAt = 0,
        status = RollingZoneDraftStatus.NEW,
    )
}
