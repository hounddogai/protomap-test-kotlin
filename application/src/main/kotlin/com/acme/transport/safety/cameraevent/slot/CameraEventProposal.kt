package com.acme.transport.safety.cameraevent.slot

import com.acme.transport.kit.text.normalize

/** Where a camera event proposal is in its life cycle. */
enum class CameraEventProposalStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a camera event proposal and its current status. */
data class CameraEventProposal(
    var code: String,
    var title: String,
    var carrier: String,
    var visible: Boolean,
    var fuelLiters: Double,
    var source: String,
    var status: CameraEventProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventProposalStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventProposalStatus): Boolean {
        when (status) {
            CameraEventProposalStatus.DRAFT -> return next == CameraEventProposalStatus.PENDING || next == CameraEventProposalStatus.CLOSED
            CameraEventProposalStatus.PENDING -> {
                when (next) {
                    CameraEventProposalStatus.SUSPENDED, CameraEventProposalStatus.CLOSED -> return true
                }
                return false
            }
            CameraEventProposalStatus.ACTIVE -> return next == CameraEventProposalStatus.SUSPENDED
            CameraEventProposalStatus.SUSPENDED -> return next == CameraEventProposalStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CameraEventProposalStatus) {
        if (!canMoveTo(next)) {
            error("camera event proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): CameraEventProposal {
        return CameraEventProposal(
            code = code,
            title = title,
            carrier = carrier,
            visible = visible,
            fuelLiters = fuelLiters,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a camera event proposal in status draft. */
fun newCameraEventProposal(code: String, title: String): CameraEventProposal {
    return CameraEventProposal(
        code = code,
        title = title,
        carrier = "",
        visible = false,
        fuelLiters = 0.0,
        source = "",
        status = CameraEventProposalStatus.DRAFT,
    )
}
