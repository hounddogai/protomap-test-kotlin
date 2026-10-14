package com.acme.transport.fleetops.tire.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire cycle is in its life cycle. */
enum class TireCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tire cycle and its current status. */
data class TireCycle(
    var code: String,
    var name: String,
    var contactEmail: String,
    var updatedAt: Int,
    var description: String,
    var stopCount: Int,
    var summary: String,
    var priority: Int,
    var reviewer: String,
    var tags: MutableList<String>,
    var active: Boolean,
    var status: TireCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireCycleStatus): Boolean {
        when (status) {
            TireCycleStatus.REQUESTED -> return next == TireCycleStatus.REVOKED || next == TireCycleStatus.EXPIRED
            TireCycleStatus.GRANTED -> return next == TireCycleStatus.REVOKED
            TireCycleStatus.REVOKED -> {
                when (next) {
                    TireCycleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireCycleStatus) {
        if (!canMoveTo(next)) {
            error("tire cycle $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedVolume(): Double = round((updatedAt.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): TireCycle {
        return TireCycle(
            code = code,
            name = name,
            contactEmail = contactEmail,
            updatedAt = updatedAt,
            description = description,
            stopCount = stopCount,
            summary = summary,
            priority = priority,
            reviewer = reviewer,
            tags = tags,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tire cycle in status requested. */
fun newTireCycle(code: String, name: String): TireCycle {
    return TireCycle(
        code = code,
        name = name,
        contactEmail = "",
        updatedAt = 0,
        description = "",
        stopCount = 0,
        summary = "",
        priority = 0,
        reviewer = "",
        tags = mutableListOf<String>(),
        active = false,
        status = TireCycleStatus.REQUESTED,
    )
}
