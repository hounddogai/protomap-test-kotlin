package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.kit.text.normalize

// The adaptive lease return draft records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a adaptive lease return draft is in its life cycle. */
enum class AdaptiveLeaseReturnDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One adaptive lease return draft, with what the planners need to act on it. */
data class AdaptiveLeaseReturnDraft(
    var code: String,
    var name: String,
    var odometer: Int,
    var trailerId: String,
    var source: String,
    var status: AdaptiveLeaseReturnDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLeaseReturnDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLeaseReturnDraftStatus): Boolean {
        when (status) {
            AdaptiveLeaseReturnDraftStatus.CREATED -> return next == AdaptiveLeaseReturnDraftStatus.VALIDATED
            AdaptiveLeaseReturnDraftStatus.VALIDATED -> return next == AdaptiveLeaseReturnDraftStatus.RETIRED
            AdaptiveLeaseReturnDraftStatus.PUBLISHED -> {
                when (next) {
                    AdaptiveLeaseReturnDraftStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveLeaseReturnDraftStatus) {
        if (!canMoveTo(next)) {
            error("adaptive lease return draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive lease return draft"} $code"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): AdaptiveLeaseReturnDraft {
        return AdaptiveLeaseReturnDraft(
            code = code,
            name = name,
            odometer = odometer,
            trailerId = trailerId,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a adaptive lease return draft in status created. */
fun newAdaptiveLeaseReturnDraft(code: String, name: String): AdaptiveLeaseReturnDraft {
    return AdaptiveLeaseReturnDraft(
        code = code,
        name = name,
        odometer = 0,
        trailerId = "",
        source = "",
        status = AdaptiveLeaseReturnDraftStatus.CREATED,
    )
}
