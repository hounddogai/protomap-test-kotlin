package com.acme.transport.customs.broker.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending broker draft is in its life cycle. */
enum class PendingBrokerDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending broker draft as the broker team keeps it. */
data class PendingBrokerDraft(
    var code: String,
    var label: String,
    var endsAt: Int,
    var active: Boolean,
    var margin: Double,
    var checksum: String,
    var status: PendingBrokerDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingBrokerDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingBrokerDraftStatus): Boolean {
        when (status) {
            PendingBrokerDraftStatus.REQUESTED -> {
                when (next) {
                    PendingBrokerDraftStatus.GRANTED -> return true
                }
                return false
            }
            PendingBrokerDraftStatus.GRANTED -> return next == PendingBrokerDraftStatus.REVOKED
            PendingBrokerDraftStatus.REVOKED -> {
                when (next) {
                    PendingBrokerDraftStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingBrokerDraftStatus) {
        if (!canMoveTo(next)) {
            error("pending broker draft $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedLoad(): Double = round((endsAt.toDouble() + margin) * 100) / 100

    fun snapshot(): PendingBrokerDraft {
        return PendingBrokerDraft(
            code = code,
            label = label,
            endsAt = endsAt,
            active = active,
            margin = margin,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending broker draft in status requested. */
fun newPendingBrokerDraft(code: String, label: String): PendingBrokerDraft {
    return PendingBrokerDraft(
        code = code,
        label = label,
        endsAt = 0,
        active = false,
        margin = 0.0,
        checksum = "",
        status = PendingBrokerDraftStatus.REQUESTED,
    )
}
