package com.acme.transport.network.node.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending node bundle is in its life cycle. */
enum class PendingNodeBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One pending node bundle, with what the planners need to act on it. */
data class PendingNodeBundle(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var ownerId: String,
    var minimum: Double,
    var expiresAt: Int,
    var status: PendingNodeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingNodeBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingNodeBundleStatus): Boolean {
        when (status) {
            PendingNodeBundleStatus.REQUESTED -> return next == PendingNodeBundleStatus.GRANTED || next == PendingNodeBundleStatus.REVOKED
            PendingNodeBundleStatus.GRANTED -> {
                when (next) {
                    PendingNodeBundleStatus.REVOKED -> return true
                }
                return false
            }
            PendingNodeBundleStatus.REVOKED -> return next == PendingNodeBundleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingNodeBundleStatus) {
        if (!canMoveTo(next)) {
            error("pending node bundle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveLoad(): Double = round((etaMinutes.toDouble() + minimum) * 100) / 100

    fun snapshot(): PendingNodeBundle {
        return PendingNodeBundle(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            ownerId = ownerId,
            minimum = minimum,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending node bundle in status requested. */
fun newPendingNodeBundle(code: String, name: String): PendingNodeBundle {
    return PendingNodeBundle(
        code = code,
        name = name,
        etaMinutes = 0,
        ownerId = "",
        minimum = 0.0,
        expiresAt = 0,
        status = PendingNodeBundleStatus.REQUESTED,
    )
}
