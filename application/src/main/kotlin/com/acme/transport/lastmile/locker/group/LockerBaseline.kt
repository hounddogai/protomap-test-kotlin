package com.acme.transport.lastmile.locker.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker baseline is in its life cycle. */
enum class LockerBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One locker baseline, with what the planners need to act on it. */
data class LockerBaseline(
    var code: String,
    var title: String,
    var endsAt: Int,
    var maximum: Double,
    var revision: Int,
    var status: LockerBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerBaselineStatus): Boolean {
        when (status) {
            LockerBaselineStatus.REQUESTED -> {
                when (next) {
                    LockerBaselineStatus.REVOKED, LockerBaselineStatus.EXPIRED -> return true
                }
                return false
            }
            LockerBaselineStatus.GRANTED -> return next == LockerBaselineStatus.EXPIRED
            LockerBaselineStatus.REVOKED -> return next == LockerBaselineStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LockerBaselineStatus) {
        if (!canMoveTo(next)) {
            error("locker baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun weightedLoad(): Double = round(revision.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): LockerBaseline {
        return LockerBaseline(
            code = code,
            title = title,
            endsAt = endsAt,
            maximum = maximum,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a locker baseline in status requested. */
fun newLockerBaseline(code: String, title: String): LockerBaseline {
    return LockerBaseline(
        code = code,
        title = title,
        endsAt = 0,
        maximum = 0.0,
        revision = 0,
        status = LockerBaselineStatus.REQUESTED,
    )
}
