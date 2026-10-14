package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker rollup is in its life cycle. */
enum class LockerRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A locker rollup. Its code is unique within a tenant. */
data class LockerRollup(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var priority: Int,
    var approved: Boolean,
    var durationMinutes: Int,
    var title: String,
    var dock: String,
    var channel: String,
    var createdAt: Int,
    var active: Boolean,
    var status: LockerRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerRollupStatus): Boolean {
        when (status) {
            LockerRollupStatus.OPEN -> {
                when (next) {
                    LockerRollupStatus.RESOLVED, LockerRollupStatus.REOPENED -> return true
                }
                return false
            }
            LockerRollupStatus.IN_REVIEW -> {
                when (next) {
                    LockerRollupStatus.REOPENED, LockerRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            LockerRollupStatus.RESOLVED -> {
                when (next) {
                    LockerRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            LockerRollupStatus.REOPENED -> {
                when (next) {
                    LockerRollupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerRollupStatus) {
        if (!canMoveTo(next)) {
            error("locker rollup $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"locker rollup"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round(durationMinutes.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): LockerRollup {
        return LockerRollup(
            code = code,
            name = name,
            tags = tags,
            priority = priority,
            approved = approved,
            durationMinutes = durationMinutes,
            title = title,
            dock = dock,
            channel = channel,
            createdAt = createdAt,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a locker rollup in status open. */
fun newLockerRollup(code: String, name: String): LockerRollup {
    return LockerRollup(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        priority = 0,
        approved = false,
        durationMinutes = 0,
        title = "",
        dock = "",
        channel = "",
        createdAt = 0,
        active = false,
        status = LockerRollupStatus.OPEN,
    )
}
