package com.acme.transport.linehaul.tender.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global tender draft is in its life cycle. */
enum class GlobalTenderDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global tender draft. Its code is unique within a tenant. */
data class GlobalTenderDraft(
    var code: String,
    var name: String,
    var title: String,
    var ratio: Double,
    var quantity: Int,
    var region: String,
    var status: GlobalTenderDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTenderDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTenderDraftStatus): Boolean {
        when (status) {
            GlobalTenderDraftStatus.PROPOSED -> return next == GlobalTenderDraftStatus.APPROVED
            GlobalTenderDraftStatus.APPROVED -> return next == GlobalTenderDraftStatus.REJECTED
            GlobalTenderDraftStatus.REJECTED -> return next == GlobalTenderDraftStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalTenderDraftStatus) {
        if (!canMoveTo(next)) {
            error("global tender draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global tender draft"} $code"

    fun effectiveVolume(): Double = round((quantity.toDouble() + ratio) * 100) / 100

    fun snapshot(): GlobalTenderDraft {
        return GlobalTenderDraft(
            code = code,
            name = name,
            title = title,
            ratio = ratio,
            quantity = quantity,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global tender draft in status proposed. */
fun newGlobalTenderDraft(code: String, name: String): GlobalTenderDraft {
    return GlobalTenderDraft(
        code = code,
        name = name,
        title = "",
        ratio = 0.0,
        quantity = 0,
        region = "",
        status = GlobalTenderDraftStatus.PROPOSED,
    )
}
