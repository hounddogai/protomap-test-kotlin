package com.acme.transport.network.node.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node digest is in its life cycle. */
enum class NodeDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A node digest. Its code is unique within a tenant. */
data class NodeDigest(
    var code: String,
    var label: String,
    var externalRef: String,
    var country: String,
    var quantity: Int,
    var maximum: Double,
    var distanceKm: Double,
    var loadKg: Double,
    var status: NodeDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeDigestStatus): Boolean {
        when (status) {
            NodeDigestStatus.NEW -> return next == NodeDigestStatus.QUEUED || next == NodeDigestStatus.DONE
            NodeDigestStatus.QUEUED -> {
                when (next) {
                    NodeDigestStatus.RUNNING, NodeDigestStatus.DONE -> return true
                }
                return false
            }
            NodeDigestStatus.RUNNING -> {
                when (next) {
                    NodeDigestStatus.DONE -> return true
                }
                return false
            }
            NodeDigestStatus.DONE -> {
                when (next) {
                    NodeDigestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeDigestStatus) {
        if (!canMoveTo(next)) {
            error("node digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedExposure(): Double = round((loadKg + quantity.toDouble()) * 100) / 100

    fun snapshot(): NodeDigest {
        return NodeDigest(
            code = code,
            label = label,
            externalRef = externalRef,
            country = country,
            quantity = quantity,
            maximum = maximum,
            distanceKm = distanceKm,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a node digest in status new. */
fun newNodeDigest(code: String, label: String): NodeDigest {
    return NodeDigest(
        code = code,
        label = label,
        externalRef = "",
        country = "",
        quantity = 0,
        maximum = 0.0,
        distanceKm = 0.0,
        loadKg = 0.0,
        status = NodeDigestStatus.NEW,
    )
}
