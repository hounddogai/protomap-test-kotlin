package com.acme.transport.linehaul.laneplan.planagreement

import com.acme.transport.kit.text.normalize

/** Where a primary lane plan draft is in its life cycle. */
enum class PrimaryLanePlanDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary lane plan draft as the lane plan team keeps it. */
data class PrimaryLanePlanDraft(
    var code: String,
    var label: String,
    var carrier: String,
    var channel: String,
    var unit: String,
    var trailerId: String,
    var checksum: String,
    var quantity: Int,
    var status: PrimaryLanePlanDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryLanePlanDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryLanePlanDraftStatus): Boolean {
        when (status) {
            PrimaryLanePlanDraftStatus.PROPOSED -> return next == PrimaryLanePlanDraftStatus.REJECTED
            PrimaryLanePlanDraftStatus.APPROVED -> {
                when (next) {
                    PrimaryLanePlanDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryLanePlanDraftStatus.REJECTED -> return next == PrimaryLanePlanDraftStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryLanePlanDraftStatus) {
        if (!canMoveTo(next)) {
            error("primary lane plan draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary lane plan draft"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): PrimaryLanePlanDraft {
        return PrimaryLanePlanDraft(
            code = code,
            label = label,
            carrier = carrier,
            channel = channel,
            unit = unit,
            trailerId = trailerId,
            checksum = checksum,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a primary lane plan draft in status proposed. */
fun newPrimaryLanePlanDraft(code: String, label: String): PrimaryLanePlanDraft {
    return PrimaryLanePlanDraft(
        code = code,
        label = label,
        carrier = "",
        channel = "",
        unit = "",
        trailerId = "",
        checksum = "",
        quantity = 0,
        status = PrimaryLanePlanDraftStatus.PROPOSED,
    )
}
