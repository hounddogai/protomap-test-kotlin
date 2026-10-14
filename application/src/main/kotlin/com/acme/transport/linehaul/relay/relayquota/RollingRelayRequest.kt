package com.acme.transport.linehaul.relay.relayquota

import com.acme.transport.kit.text.normalize

/** Where a rolling relay request is in its life cycle. */
enum class RollingRelayRequestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One rolling relay request, with what the planners need to act on it. */
data class RollingRelayRequest(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var reviewer: String,
    var title: String,
    var floorValue: Double,
    var status: RollingRelayRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRelayRequestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRelayRequestStatus): Boolean {
        when (status) {
            RollingRelayRequestStatus.NEW -> return next == RollingRelayRequestStatus.FAILED
            RollingRelayRequestStatus.QUEUED -> return next == RollingRelayRequestStatus.DONE || next == RollingRelayRequestStatus.FAILED
            RollingRelayRequestStatus.RUNNING -> {
                when (next) {
                    RollingRelayRequestStatus.DONE, RollingRelayRequestStatus.FAILED -> return true
                }
                return false
            }
            RollingRelayRequestStatus.DONE -> {
                when (next) {
                    RollingRelayRequestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingRelayRequestStatus) {
        if (!canMoveTo(next)) {
            error("rolling relay request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = floorValue

    fun snapshot(): RollingRelayRequest {
        return RollingRelayRequest(
            code = code,
            label = label,
            tags = tags,
            reviewer = reviewer,
            title = title,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling relay request in status new. */
fun newRollingRelayRequest(code: String, label: String): RollingRelayRequest {
    return RollingRelayRequest(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        reviewer = "",
        title = "",
        floorValue = 0.0,
        status = RollingRelayRequestStatus.NEW,
    )
}
