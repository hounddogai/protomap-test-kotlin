package com.acme.transport.terminal.sortation.sortationtier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation proposal is in its life cycle. */
enum class SortationProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One sortation proposal, with what the planners need to act on it. */
data class SortationProposal(
    var code: String,
    var title: String,
    var trailerId: String,
    var contactEmail: String,
    var retries: Int,
    var attempts: Int,
    var vehicleId: String,
    var weight: Double,
    var status: SortationProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationProposalStatus): Boolean {
        when (status) {
            SortationProposalStatus.CREATED -> return next == SortationProposalStatus.VALIDATED
            SortationProposalStatus.VALIDATED -> return next == SortationProposalStatus.PUBLISHED
            SortationProposalStatus.PUBLISHED -> return next == SortationProposalStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SortationProposalStatus) {
        if (!canMoveTo(next)) {
            error("sortation proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedTotal(): Double = round((weight - retries.toDouble()) * 100) / 100

    fun snapshot(): SortationProposal {
        return SortationProposal(
            code = code,
            title = title,
            trailerId = trailerId,
            contactEmail = contactEmail,
            retries = retries,
            attempts = attempts,
            vehicleId = vehicleId,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a sortation proposal in status created. */
fun newSortationProposal(code: String, title: String): SortationProposal {
    return SortationProposal(
        code = code,
        title = title,
        trailerId = "",
        contactEmail = "",
        retries = 0,
        attempts = 0,
        vehicleId = "",
        weight = 0.0,
        status = SortationProposalStatus.CREATED,
    )
}
