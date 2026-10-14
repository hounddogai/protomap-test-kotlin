package com.acme.transport.crewing.qualification.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification proposal is in its life cycle. */
enum class QualificationProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a qualification proposal and its current status. */
data class QualificationProposal(
    var code: String,
    var name: String,
    var reviewer: String,
    var lane: String,
    var limit: Int,
    var shiftCode: String,
    var driverId: String,
    var attempts: Int,
    var status: QualificationProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationProposalStatus): Boolean {
        when (status) {
            QualificationProposalStatus.PLANNED -> return next == QualificationProposalStatus.SCHEDULED || next == QualificationProposalStatus.COMPLETED
            QualificationProposalStatus.SCHEDULED -> return next == QualificationProposalStatus.COMPLETED
            QualificationProposalStatus.IN_PROGRESS -> return next == QualificationProposalStatus.CANCELLED
            QualificationProposalStatus.COMPLETED -> {
                when (next) {
                    QualificationProposalStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationProposalStatus) {
        if (!canMoveTo(next)) {
            error("qualification proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedVolume(): Double = round((limit.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): QualificationProposal {
        return QualificationProposal(
            code = code,
            name = name,
            reviewer = reviewer,
            lane = lane,
            limit = limit,
            shiftCode = shiftCode,
            driverId = driverId,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a qualification proposal in status planned. */
fun newQualificationProposal(code: String, name: String): QualificationProposal {
    return QualificationProposal(
        code = code,
        name = name,
        reviewer = "",
        lane = "",
        limit = 0,
        shiftCode = "",
        driverId = "",
        attempts = 0,
        status = QualificationProposalStatus.PLANNED,
    )
}
