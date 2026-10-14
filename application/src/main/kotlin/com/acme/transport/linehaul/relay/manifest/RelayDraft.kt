package com.acme.transport.linehaul.relay.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay draft is in its life cycle. */
enum class RelayDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A relay draft. Its code is unique within a tenant. */
data class RelayDraft(
    var code: String,
    var title: String,
    var capacity: Int,
    var visible: Boolean,
    var fuelLiters: Double,
    var maximum: Double,
    var status: RelayDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayDraftStatus): Boolean {
        when (status) {
            RelayDraftStatus.PROPOSED -> return next == RelayDraftStatus.APPROVED
            RelayDraftStatus.APPROVED -> return next == RelayDraftStatus.REJECTED
            RelayDraftStatus.REJECTED -> return next == RelayDraftStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RelayDraftStatus) {
        if (!canMoveTo(next)) {
            error("relay draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round(fuelLiters * capacity.toDouble() * 100) / 100

    fun snapshot(): RelayDraft {
        return RelayDraft(
            code = code,
            title = title,
            capacity = capacity,
            visible = visible,
            fuelLiters = fuelLiters,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a relay draft in status proposed. */
fun newRelayDraft(code: String, title: String): RelayDraft {
    return RelayDraft(
        code = code,
        title = title,
        capacity = 0,
        visible = false,
        fuelLiters = 0.0,
        maximum = 0.0,
        status = RelayDraftStatus.PROPOSED,
    )
}
