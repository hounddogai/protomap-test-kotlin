package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped service area draft records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a capped service area draft is in its life cycle. */
enum class CappedServiceAreaDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a capped service area draft and its current status. */
data class CappedServiceAreaDraft(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var maximum: Double,
    var durationMinutes: Int,
    var carrier: String,
    var status: CappedServiceAreaDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedServiceAreaDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedServiceAreaDraftStatus): Boolean {
        when (status) {
            CappedServiceAreaDraftStatus.REQUESTED -> return next == CappedServiceAreaDraftStatus.EXPIRED
            CappedServiceAreaDraftStatus.GRANTED -> return next == CappedServiceAreaDraftStatus.REVOKED
            CappedServiceAreaDraftStatus.REVOKED -> return next == CappedServiceAreaDraftStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedServiceAreaDraftStatus) {
        if (!canMoveTo(next)) {
            error("capped service area draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"capped service area draft"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round((durationMinutes.toDouble() + maximum) * 100) / 100

    fun snapshot(): CappedServiceAreaDraft {
        return CappedServiceAreaDraft(
            code = code,
            label = label,
            attributes = attributes,
            maximum = maximum,
            durationMinutes = durationMinutes,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped service area draft in status requested. */
fun newCappedServiceAreaDraft(code: String, label: String): CappedServiceAreaDraft {
    return CappedServiceAreaDraft(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        maximum = 0.0,
        durationMinutes = 0,
        carrier = "",
        status = CappedServiceAreaDraftStatus.REQUESTED,
    )
}
