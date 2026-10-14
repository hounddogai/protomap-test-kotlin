package com.acme.transport.network.node.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The node manifest records of network's node work, and the rules, stores, and calculations around them.

/** Where a node manifest is in its life cycle. */
enum class NodeManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One node manifest, with what the planners need to act on it. */
data class NodeManifest(
    var code: String,
    var title: String,
    var ownerId: String,
    var limit: Int,
    var loadKg: Double,
    var visible: Boolean,
    var endsAt: Int,
    var durationMinutes: Int,
    var status: NodeManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeManifestStatus): Boolean {
        when (status) {
            NodeManifestStatus.DRAFT -> {
                when (next) {
                    NodeManifestStatus.PENDING, NodeManifestStatus.SUSPENDED -> return true
                }
                return false
            }
            NodeManifestStatus.PENDING -> {
                when (next) {
                    NodeManifestStatus.ACTIVE -> return true
                }
                return false
            }
            NodeManifestStatus.ACTIVE -> return next == NodeManifestStatus.CLOSED
            NodeManifestStatus.SUSPENDED -> return next == NodeManifestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: NodeManifestStatus) {
        if (!canMoveTo(next)) {
            error("node manifest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedTotal(): Double = round(loadKg * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): NodeManifest {
        return NodeManifest(
            code = code,
            title = title,
            ownerId = ownerId,
            limit = limit,
            loadKg = loadKg,
            visible = visible,
            endsAt = endsAt,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a node manifest in status draft. */
fun newNodeManifest(code: String, title: String): NodeManifest {
    return NodeManifest(
        code = code,
        title = title,
        ownerId = "",
        limit = 0,
        loadKg = 0.0,
        visible = false,
        endsAt = 0,
        durationMinutes = 0,
        status = NodeManifestStatus.DRAFT,
    )
}
