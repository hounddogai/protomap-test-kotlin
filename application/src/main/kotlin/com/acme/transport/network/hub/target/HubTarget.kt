package com.acme.transport.network.hub.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hub target records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub target is in its life cycle. */
enum class HubTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One hub target, with what the planners need to act on it. */
data class HubTarget(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var routeCode: String,
    var externalRef: String,
    var expiresAt: Int,
    var dock: String,
    var status: HubTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubTargetStatus): Boolean {
        when (status) {
            HubTargetStatus.DRAFT -> {
                when (next) {
                    HubTargetStatus.PENDING, HubTargetStatus.CLOSED -> return true
                }
                return false
            }
            HubTargetStatus.PENDING -> {
                when (next) {
                    HubTargetStatus.ACTIVE, HubTargetStatus.CLOSED -> return true
                }
                return false
            }
            HubTargetStatus.ACTIVE -> {
                when (next) {
                    HubTargetStatus.SUSPENDED, HubTargetStatus.CLOSED -> return true
                }
                return false
            }
            HubTargetStatus.SUSPENDED -> return next == HubTargetStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: HubTargetStatus) {
        if (!canMoveTo(next)) {
            error("hub target $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"hub target"} $code"

    fun weightedVolume(): Double = round((etaMinutes.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): HubTarget {
        return HubTarget(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            routeCode = routeCode,
            externalRef = externalRef,
            expiresAt = expiresAt,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hub target in status draft. */
fun newHubTarget(code: String, title: String): HubTarget {
    return HubTarget(
        code = code,
        title = title,
        etaMinutes = 0,
        routeCode = "",
        externalRef = "",
        expiresAt = 0,
        dock = "",
        status = HubTargetStatus.DRAFT,
    )
}
