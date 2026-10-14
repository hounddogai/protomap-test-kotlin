package com.acme.transport.network.node.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node mapping is in its life cycle. */
enum class NodeMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A node mapping as the node team keeps it. */
data class NodeMapping(
    var code: String,
    var name: String,
    var label: String,
    var capacity: Int,
    var amount: Double,
    var score: Double,
    var revision: Int,
    var lane: String,
    var source: String,
    var status: NodeMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeMappingStatus): Boolean {
        when (status) {
            NodeMappingStatus.CREATED -> {
                when (next) {
                    NodeMappingStatus.RETIRED -> return true
                }
                return false
            }
            NodeMappingStatus.VALIDATED -> return next == NodeMappingStatus.PUBLISHED || next == NodeMappingStatus.RETIRED
            NodeMappingStatus.PUBLISHED -> {
                when (next) {
                    NodeMappingStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeMappingStatus) {
        if (!canMoveTo(next)) {
            error("node mapping $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"node mapping"} $code"

    fun projectedLoad(): Double = round((score - capacity.toDouble()) * 100) / 100

    fun snapshot(): NodeMapping {
        return NodeMapping(
            code = code,
            name = name,
            label = label,
            capacity = capacity,
            amount = amount,
            score = score,
            revision = revision,
            lane = lane,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a node mapping in status created. */
fun newNodeMapping(code: String, name: String): NodeMapping {
    return NodeMapping(
        code = code,
        name = name,
        label = "",
        capacity = 0,
        amount = 0.0,
        score = 0.0,
        revision = 0,
        lane = "",
        source = "",
        status = NodeMappingStatus.CREATED,
    )
}
