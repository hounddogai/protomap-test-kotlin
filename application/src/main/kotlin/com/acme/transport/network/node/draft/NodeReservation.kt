package com.acme.transport.network.node.draft

import com.acme.transport.kit.text.normalize

/** Where a node reservation is in its life cycle. */
enum class NodeReservationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a node reservation and its current status. */
data class NodeReservation(
    var code: String,
    var title: String,
    var carrier: String,
    var weight: Double,
    var label: String,
    var visible: Boolean,
    var status: NodeReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeReservationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeReservationStatus): Boolean {
        when (status) {
            NodeReservationStatus.CREATED -> return next == NodeReservationStatus.RETIRED
            NodeReservationStatus.VALIDATED -> {
                when (next) {
                    NodeReservationStatus.PUBLISHED, NodeReservationStatus.RETIRED -> return true
                }
                return false
            }
            NodeReservationStatus.PUBLISHED -> {
                when (next) {
                    NodeReservationStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeReservationStatus) {
        if (!canMoveTo(next)) {
            error("node reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($title)"

    fun measuredValue(): Double = weight

    fun snapshot(): NodeReservation {
        return NodeReservation(
            code = code,
            title = title,
            carrier = carrier,
            weight = weight,
            label = label,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a node reservation in status created. */
fun newNodeReservation(code: String, title: String): NodeReservation {
    return NodeReservation(
        code = code,
        title = title,
        carrier = "",
        weight = 0.0,
        label = "",
        visible = false,
        status = NodeReservationStatus.CREATED,
    )
}
