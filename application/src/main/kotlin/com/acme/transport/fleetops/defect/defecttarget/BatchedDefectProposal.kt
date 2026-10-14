package com.acme.transport.fleetops.defect.defecttarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched defect proposal is in its life cycle. */
enum class BatchedDefectProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A batched defect proposal. Its code is unique within a tenant. */
data class BatchedDefectProposal(
    var code: String,
    var name: String,
    var phone: String,
    var maximum: Double,
    var threshold: Double,
    var windowDays: Int,
    var etaMinutes: Int,
    var margin: Double,
    var country: String,
    var shiftCode: String,
    var status: BatchedDefectProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDefectProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDefectProposalStatus): Boolean {
        when (status) {
            BatchedDefectProposalStatus.REQUESTED -> {
                when (next) {
                    BatchedDefectProposalStatus.GRANTED -> return true
                }
                return false
            }
            BatchedDefectProposalStatus.GRANTED -> return next == BatchedDefectProposalStatus.EXPIRED
            BatchedDefectProposalStatus.REVOKED -> {
                when (next) {
                    BatchedDefectProposalStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedDefectProposalStatus) {
        if (!canMoveTo(next)) {
            error("batched defect proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round((margin + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): BatchedDefectProposal {
        return BatchedDefectProposal(
            code = code,
            name = name,
            phone = phone,
            maximum = maximum,
            threshold = threshold,
            windowDays = windowDays,
            etaMinutes = etaMinutes,
            margin = margin,
            country = country,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched defect proposal in status requested. */
fun newBatchedDefectProposal(code: String, name: String): BatchedDefectProposal {
    return BatchedDefectProposal(
        code = code,
        name = name,
        phone = "",
        maximum = 0.0,
        threshold = 0.0,
        windowDays = 0,
        etaMinutes = 0,
        margin = 0.0,
        country = "",
        shiftCode = "",
        status = BatchedDefectProposalStatus.REQUESTED,
    )
}
