package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss proposal is in its life cycle. */
enum class NearMissProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A near miss proposal as the near miss team keeps it. */
data class NearMissProposal(
    var code: String,
    var label: String,
    var loadKg: Double,
    var channel: String,
    var startsAt: Int,
    var durationMinutes: Int,
    var endsAt: Int,
    var routeCode: String,
    var region: String,
    var attributes: MutableMap<String, String>,
    var phone: String,
    var status: NearMissProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissProposalStatus): Boolean {
        when (status) {
            NearMissProposalStatus.OPEN -> return next == NearMissProposalStatus.IN_REVIEW || next == NearMissProposalStatus.ARCHIVED
            NearMissProposalStatus.IN_REVIEW -> return next == NearMissProposalStatus.ARCHIVED
            NearMissProposalStatus.RESOLVED -> return next == NearMissProposalStatus.ARCHIVED
            NearMissProposalStatus.REOPENED -> {
                when (next) {
                    NearMissProposalStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissProposalStatus) {
        if (!canMoveTo(next)) {
            error("near miss proposal $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round(endsAt.toDouble() * loadKg * 100) / 100

    fun snapshot(): NearMissProposal {
        return NearMissProposal(
            code = code,
            label = label,
            loadKg = loadKg,
            channel = channel,
            startsAt = startsAt,
            durationMinutes = durationMinutes,
            endsAt = endsAt,
            routeCode = routeCode,
            region = region,
            attributes = attributes,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a near miss proposal in status open. */
fun newNearMissProposal(code: String, label: String): NearMissProposal {
    return NearMissProposal(
        code = code,
        label = label,
        loadKg = 0.0,
        channel = "",
        startsAt = 0,
        durationMinutes = 0,
        endsAt = 0,
        routeCode = "",
        region = "",
        attributes = mutableMapOf<String, String>(),
        phone = "",
        status = NearMissProposalStatus.OPEN,
    )
}
