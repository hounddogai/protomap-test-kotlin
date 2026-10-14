package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node record is in its life cycle. */
enum class NodeRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A node record. Its code is unique within a tenant. */
data class NodeRecord(
    var code: String,
    var name: String,
    var capacity: Int,
    var vehicleId: String,
    var batchSize: Int,
    var etaMinutes: Int,
    var status: NodeRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeRecordStatus): Boolean {
        when (status) {
            NodeRecordStatus.NEW -> {
                when (next) {
                    NodeRecordStatus.FAILED -> return true
                }
                return false
            }
            NodeRecordStatus.QUEUED -> return next == NodeRecordStatus.DONE || next == NodeRecordStatus.FAILED
            NodeRecordStatus.RUNNING -> {
                when (next) {
                    NodeRecordStatus.DONE, NodeRecordStatus.FAILED -> return true
                }
                return false
            }
            NodeRecordStatus.DONE -> {
                when (next) {
                    NodeRecordStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeRecordStatus) {
        if (!canMoveTo(next)) {
            error("node record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round((etaMinutes.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): NodeRecord {
        return NodeRecord(
            code = code,
            name = name,
            capacity = capacity,
            vehicleId = vehicleId,
            batchSize = batchSize,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a node record in status new. */
fun newNodeRecord(code: String, name: String): NodeRecord {
    return NodeRecord(
        code = code,
        name = name,
        capacity = 0,
        vehicleId = "",
        batchSize = 0,
        etaMinutes = 0,
        status = NodeRecordStatus.NEW,
    )
}
