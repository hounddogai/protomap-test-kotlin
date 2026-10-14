package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub group is in its life cycle. */
enum class HubGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A hub group as the hub team keeps it. */
data class HubGroup(
    var code: String,
    var title: String,
    var score: Double,
    var limit: Int,
    var expiresAt: Int,
    var budget: Double,
    var status: HubGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubGroupStatus): Boolean {
        when (status) {
            HubGroupStatus.NEW -> return next == HubGroupStatus.DONE
            HubGroupStatus.QUEUED -> return next == HubGroupStatus.DONE
            HubGroupStatus.RUNNING -> return next == HubGroupStatus.DONE || next == HubGroupStatus.FAILED
            HubGroupStatus.DONE -> {
                when (next) {
                    HubGroupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubGroupStatus) {
        if (!canMoveTo(next)) {
            error("hub group $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveLoad(): Double = round((score + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): HubGroup {
        return HubGroup(
            code = code,
            title = title,
            score = score,
            limit = limit,
            expiresAt = expiresAt,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hub group in status new. */
fun newHubGroup(code: String, title: String): HubGroup {
    return HubGroup(
        code = code,
        title = title,
        score = 0.0,
        limit = 0,
        expiresAt = 0,
        budget = 0.0,
        status = HubGroupStatus.NEW,
    )
}
