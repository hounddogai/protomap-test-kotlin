package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node notice is in its life cycle. */
enum class NodeNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One node notice, with what the planners need to act on it. */
data class NodeNotice(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var source: String,
    var durationMinutes: Int,
    var status: NodeNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeNoticeStatus): Boolean {
        when (status) {
            NodeNoticeStatus.PROPOSED -> {
                when (next) {
                    NodeNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
            NodeNoticeStatus.APPROVED -> return next == NodeNoticeStatus.REJECTED || next == NodeNoticeStatus.WITHDRAWN
            NodeNoticeStatus.REJECTED -> {
                when (next) {
                    NodeNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeNoticeStatus) {
        if (!canMoveTo(next)) {
            error("node notice $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun weightedVolume(): Double = round(expiresAt.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): NodeNotice {
        return NodeNotice(
            code = code,
            name = name,
            expiresAt = expiresAt,
            source = source,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a node notice in status proposed. */
fun newNodeNotice(code: String, name: String): NodeNotice {
    return NodeNotice(
        code = code,
        name = name,
        expiresAt = 0,
        source = "",
        durationMinutes = 0,
        status = NodeNoticeStatus.PROPOSED,
    )
}
