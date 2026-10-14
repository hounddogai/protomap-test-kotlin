package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered hold draft is in its life cycle. */
enum class TieredHoldDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a tiered hold draft and its current status. */
data class TieredHoldDraft(
    var code: String,
    var label: String,
    var loadKg: Double,
    var discount: Double,
    var externalRef: String,
    var region: String,
    var approved: Boolean,
    var status: TieredHoldDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredHoldDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredHoldDraftStatus): Boolean {
        when (status) {
            TieredHoldDraftStatus.PROPOSED -> {
                when (next) {
                    TieredHoldDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredHoldDraftStatus.APPROVED -> return next == TieredHoldDraftStatus.WITHDRAWN
            TieredHoldDraftStatus.REJECTED -> return next == TieredHoldDraftStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredHoldDraftStatus) {
        if (!canMoveTo(next)) {
            error("tiered hold draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedValue(): Double = round((loadKg - discount) * 100) / 100

    fun snapshot(): TieredHoldDraft {
        return TieredHoldDraft(
            code = code,
            label = label,
            loadKg = loadKg,
            discount = discount,
            externalRef = externalRef,
            region = region,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tiered hold draft in status proposed. */
fun newTieredHoldDraft(code: String, label: String): TieredHoldDraft {
    return TieredHoldDraft(
        code = code,
        label = label,
        loadKg = 0.0,
        discount = 0.0,
        externalRef = "",
        region = "",
        approved = false,
        status = TieredHoldDraftStatus.PROPOSED,
    )
}
