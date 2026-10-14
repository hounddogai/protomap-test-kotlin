package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.kit.text.normalize

/** Where a rolling drop digest is in its life cycle. */
enum class RollingDropDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One rolling drop digest, with what the planners need to act on it. */
data class RollingDropDigest(
    var code: String,
    var name: String,
    var dock: String,
    var ceiling: Double,
    var carrier: String,
    var status: RollingDropDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDropDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDropDigestStatus): Boolean {
        when (status) {
            RollingDropDigestStatus.IDLE -> return next == RollingDropDigestStatus.WARMING || next == RollingDropDigestStatus.STOPPED
            RollingDropDigestStatus.WARMING -> {
                when (next) {
                    RollingDropDigestStatus.DRAINING, RollingDropDigestStatus.STOPPED -> return true
                }
                return false
            }
            RollingDropDigestStatus.READY -> {
                when (next) {
                    RollingDropDigestStatus.STOPPED -> return true
                }
                return false
            }
            RollingDropDigestStatus.DRAINING -> {
                when (next) {
                    RollingDropDigestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDropDigestStatus) {
        if (!canMoveTo(next)) {
            error("rolling drop digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling drop digest"} $code"

    fun measuredValue(): Double = ceiling

    fun snapshot(): RollingDropDigest {
        return RollingDropDigest(
            code = code,
            name = name,
            dock = dock,
            ceiling = ceiling,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling drop digest in status idle. */
fun newRollingDropDigest(code: String, name: String): RollingDropDigest {
    return RollingDropDigest(
        code = code,
        name = name,
        dock = "",
        ceiling = 0.0,
        carrier = "",
        status = RollingDropDigestStatus.IDLE,
    )
}
