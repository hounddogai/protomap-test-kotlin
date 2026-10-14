package com.acme.transport.safety.cameraevent.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending camera event proposal is in its life cycle. */
enum class PendingCameraEventProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a pending camera event proposal and its current status. */
data class PendingCameraEventProposal(
    var code: String,
    var name: String,
    var capacity: Int,
    var stopCount: Int,
    var lane: String,
    var visible: Boolean,
    var ceiling: Double,
    var shiftCode: String,
    var trailerId: String,
    var attributes: MutableMap<String, String>,
    var pinned: Boolean,
    var status: PendingCameraEventProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCameraEventProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCameraEventProposalStatus): Boolean {
        when (status) {
            PendingCameraEventProposalStatus.CREATED -> {
                when (next) {
                    PendingCameraEventProposalStatus.VALIDATED -> return true
                }
                return false
            }
            PendingCameraEventProposalStatus.VALIDATED -> return next == PendingCameraEventProposalStatus.PUBLISHED || next == PendingCameraEventProposalStatus.RETIRED
            PendingCameraEventProposalStatus.PUBLISHED -> {
                when (next) {
                    PendingCameraEventProposalStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingCameraEventProposalStatus) {
        if (!canMoveTo(next)) {
            error("pending camera event proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round(stopCount.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): PendingCameraEventProposal {
        return PendingCameraEventProposal(
            code = code,
            name = name,
            capacity = capacity,
            stopCount = stopCount,
            lane = lane,
            visible = visible,
            ceiling = ceiling,
            shiftCode = shiftCode,
            trailerId = trailerId,
            attributes = attributes,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a pending camera event proposal in status created. */
fun newPendingCameraEventProposal(code: String, name: String): PendingCameraEventProposal {
    return PendingCameraEventProposal(
        code = code,
        name = name,
        capacity = 0,
        stopCount = 0,
        lane = "",
        visible = false,
        ceiling = 0.0,
        shiftCode = "",
        trailerId = "",
        attributes = mutableMapOf<String, String>(),
        pinned = false,
        status = PendingCameraEventProposalStatus.CREATED,
    )
}
