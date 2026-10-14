package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global trailer proposal is in its life cycle. */
enum class GlobalTrailerProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One global trailer proposal, with what the planners need to act on it. */
data class GlobalTrailerProposal(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var fuelLiters: Double,
    var sequence: Int,
    var timezone: String,
    var expiresAt: Int,
    var createdAt: Int,
    var durationMinutes: Int,
    var title: String,
    var vehicleId: String,
    var status: GlobalTrailerProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTrailerProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTrailerProposalStatus): Boolean {
        when (status) {
            GlobalTrailerProposalStatus.OPEN -> {
                when (next) {
                    GlobalTrailerProposalStatus.RESOLVED -> return true
                }
                return false
            }
            GlobalTrailerProposalStatus.IN_REVIEW -> {
                when (next) {
                    GlobalTrailerProposalStatus.REOPENED -> return true
                }
                return false
            }
            GlobalTrailerProposalStatus.RESOLVED -> {
                when (next) {
                    GlobalTrailerProposalStatus.REOPENED, GlobalTrailerProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalTrailerProposalStatus.REOPENED -> {
                when (next) {
                    GlobalTrailerProposalStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalTrailerProposalStatus) {
        if (!canMoveTo(next)) {
            error("global trailer proposal $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round((expiresAt.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): GlobalTrailerProposal {
        return GlobalTrailerProposal(
            code = code,
            name = name,
            attributes = attributes,
            fuelLiters = fuelLiters,
            sequence = sequence,
            timezone = timezone,
            expiresAt = expiresAt,
            createdAt = createdAt,
            durationMinutes = durationMinutes,
            title = title,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global trailer proposal in status open. */
fun newGlobalTrailerProposal(code: String, name: String): GlobalTrailerProposal {
    return GlobalTrailerProposal(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        fuelLiters = 0.0,
        sequence = 0,
        timezone = "",
        expiresAt = 0,
        createdAt = 0,
        durationMinutes = 0,
        title = "",
        vehicleId = "",
        status = GlobalTrailerProposalStatus.OPEN,
    )
}
