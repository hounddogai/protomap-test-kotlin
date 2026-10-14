package com.acme.transport.linehaul.laneplan.planthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending lane plan draft is in its life cycle. */
enum class PendingLanePlanDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending lane plan draft as the lane plan team keeps it. */
data class PendingLanePlanDraft(
    var code: String,
    var name: String,
    var loadKg: Double,
    var externalRef: String,
    var batchSize: Int,
    var archived: Boolean,
    var status: PendingLanePlanDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingLanePlanDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingLanePlanDraftStatus): Boolean {
        when (status) {
            PendingLanePlanDraftStatus.REQUESTED -> {
                when (next) {
                    PendingLanePlanDraftStatus.EXPIRED -> return true
                }
                return false
            }
            PendingLanePlanDraftStatus.GRANTED -> {
                when (next) {
                    PendingLanePlanDraftStatus.REVOKED, PendingLanePlanDraftStatus.EXPIRED -> return true
                }
                return false
            }
            PendingLanePlanDraftStatus.REVOKED -> return next == PendingLanePlanDraftStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingLanePlanDraftStatus) {
        if (!canMoveTo(next)) {
            error("pending lane plan draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round(batchSize.toDouble() * loadKg * 100) / 100

    fun snapshot(): PendingLanePlanDraft {
        return PendingLanePlanDraft(
            code = code,
            name = name,
            loadKg = loadKg,
            externalRef = externalRef,
            batchSize = batchSize,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending lane plan draft in status requested. */
fun newPendingLanePlanDraft(code: String, name: String): PendingLanePlanDraft {
    return PendingLanePlanDraft(
        code = code,
        name = name,
        loadKg = 0.0,
        externalRef = "",
        batchSize = 0,
        archived = false,
        status = PendingLanePlanDraftStatus.REQUESTED,
    )
}
