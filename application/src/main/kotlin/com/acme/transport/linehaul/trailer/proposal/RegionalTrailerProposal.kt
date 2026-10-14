package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional trailer proposal records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a regional trailer proposal is in its life cycle. */
enum class RegionalTrailerProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional trailer proposal as the trailer team keeps it. */
data class RegionalTrailerProposal(
    var code: String,
    var name: String,
    var capacity: Int,
    var pinned: Boolean,
    var country: String,
    var threshold: Double,
    var loadKg: Double,
    var fuelLiters: Double,
    var checksum: String,
    var status: RegionalTrailerProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTrailerProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTrailerProposalStatus): Boolean {
        when (status) {
            RegionalTrailerProposalStatus.CREATED -> return next == RegionalTrailerProposalStatus.VALIDATED || next == RegionalTrailerProposalStatus.PUBLISHED
            RegionalTrailerProposalStatus.VALIDATED -> return next == RegionalTrailerProposalStatus.PUBLISHED || next == RegionalTrailerProposalStatus.RETIRED
            RegionalTrailerProposalStatus.PUBLISHED -> {
                when (next) {
                    RegionalTrailerProposalStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalTrailerProposalStatus) {
        if (!canMoveTo(next)) {
            error("regional trailer proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional trailer proposal"} $code"

    fun projectedVolume(): Double = round(threshold * loadKg * 100) / 100

    fun snapshot(): RegionalTrailerProposal {
        return RegionalTrailerProposal(
            code = code,
            name = name,
            capacity = capacity,
            pinned = pinned,
            country = country,
            threshold = threshold,
            loadKg = loadKg,
            fuelLiters = fuelLiters,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a regional trailer proposal in status created. */
fun newRegionalTrailerProposal(code: String, name: String): RegionalTrailerProposal {
    return RegionalTrailerProposal(
        code = code,
        name = name,
        capacity = 0,
        pinned = false,
        country = "",
        threshold = 0.0,
        loadKg = 0.0,
        fuelLiters = 0.0,
        checksum = "",
        status = RegionalTrailerProposalStatus.CREATED,
    )
}
