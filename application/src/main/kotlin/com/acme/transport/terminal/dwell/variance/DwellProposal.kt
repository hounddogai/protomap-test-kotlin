package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell proposal is in its life cycle. */
enum class DwellProposalStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a dwell proposal and its current status. */
data class DwellProposal(
    var code: String,
    var title: String,
    var cost: Double,
    var amount: Double,
    var fuelLiters: Double,
    var contactEmail: String,
    var priority: Int,
    var weight: Double,
    var discount: Double,
    var status: DwellProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellProposalStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellProposalStatus): Boolean {
        when (status) {
            DwellProposalStatus.NEW -> return next == DwellProposalStatus.QUEUED
            DwellProposalStatus.QUEUED -> {
                when (next) {
                    DwellProposalStatus.RUNNING, DwellProposalStatus.DONE -> return true
                }
                return false
            }
            DwellProposalStatus.RUNNING -> return next == DwellProposalStatus.DONE || next == DwellProposalStatus.FAILED
            DwellProposalStatus.DONE -> {
                when (next) {
                    DwellProposalStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellProposalStatus) {
        if (!canMoveTo(next)) {
            error("dwell proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedValue(): Double = round((cost + weight) * 100) / 100

    fun snapshot(): DwellProposal {
        return DwellProposal(
            code = code,
            title = title,
            cost = cost,
            amount = amount,
            fuelLiters = fuelLiters,
            contactEmail = contactEmail,
            priority = priority,
            weight = weight,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dwell proposal in status new. */
fun newDwellProposal(code: String, title: String): DwellProposal {
    return DwellProposal(
        code = code,
        title = title,
        cost = 0.0,
        amount = 0.0,
        fuelLiters = 0.0,
        contactEmail = "",
        priority = 0,
        weight = 0.0,
        discount = 0.0,
        status = DwellProposalStatus.NEW,
    )
}
