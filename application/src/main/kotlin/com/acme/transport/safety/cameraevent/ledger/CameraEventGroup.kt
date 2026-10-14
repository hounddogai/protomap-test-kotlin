package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.text.normalize

/** Where a camera event group is in its life cycle. */
enum class CameraEventGroupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a camera event group and its current status. */
data class CameraEventGroup(
    var code: String,
    var name: String,
    var trailerId: String,
    var ratio: Double,
    var dock: String,
    var channel: String,
    var status: CameraEventGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventGroupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventGroupStatus): Boolean {
        when (status) {
            CameraEventGroupStatus.IDLE -> {
                when (next) {
                    CameraEventGroupStatus.STOPPED -> return true
                }
                return false
            }
            CameraEventGroupStatus.WARMING -> {
                when (next) {
                    CameraEventGroupStatus.READY, CameraEventGroupStatus.DRAINING -> return true
                }
                return false
            }
            CameraEventGroupStatus.READY -> {
                when (next) {
                    CameraEventGroupStatus.STOPPED -> return true
                }
                return false
            }
            CameraEventGroupStatus.DRAINING -> {
                when (next) {
                    CameraEventGroupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventGroupStatus) {
        if (!canMoveTo(next)) {
            error("camera event group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"camera event group"} $code"

    fun measuredValue(): Double = ratio

    fun snapshot(): CameraEventGroup {
        return CameraEventGroup(
            code = code,
            name = name,
            trailerId = trailerId,
            ratio = ratio,
            dock = dock,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a camera event group in status idle. */
fun newCameraEventGroup(code: String, name: String): CameraEventGroup {
    return CameraEventGroup(
        code = code,
        name = name,
        trailerId = "",
        ratio = 0.0,
        dock = "",
        channel = "",
        status = CameraEventGroupStatus.IDLE,
    )
}
